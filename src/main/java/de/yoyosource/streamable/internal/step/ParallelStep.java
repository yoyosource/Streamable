package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.internal.FinishException;
import de.yoyosource.streamable.internal.Sequence;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.Element;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ParallelStep extends Step {

    private AtomicLong processing = new AtomicLong();

    private final AtomicBoolean processingIntermediateResults = new AtomicBoolean();

    private final Queue<Element.Value<Consumer<Long>>> queue = new LinkedList<>();

    private long finish = Integer.MAX_VALUE;
    private long counter = 0;

    private final Map<Long, Object> containers = new HashMap<>();

    private final AtomicLong index = new AtomicLong();
    private final Sequence results = new Sequence();

    public ParallelStep(StreamableGatherer streamableGatherer, int maxParallelTasks) {
        super(streamableGatherer);
        if (maxParallelTasks == 0) {
            throw new IllegalArgumentException("maxParallelTasks must be greater than 0");
        }
        for (int i = 0; i < maxParallelTasks; i++) {
            new WorkerThread();
        }
    }

    @Override
    public void consume(Element element) {
        if (counter > finish) throw new FinishException();

        if (element instanceof Element.Value<?> value) {
            Sequence.Inserter result = results.inserter();
            synchronized (queue) {
                queue.add(new Element.Value<>(counter++, __ -> processValue(value, result)));
            }
        } else if (element instanceof Element.Finish<?>) {
            synchronized (queue) {
                finish = Math.min(finish, counter);
                queue.add(new Element.Value<>(counter++, this::processFinish));
            }
        }
    }

    private class WorkerThread extends Thread {

        public WorkerThread() {
            setDaemon(true);
            start();
        }

        @Override
        public void run() {
            while (true) {
                Element.Value<Consumer<Long>> value;
                synchronized (queue) {
                    if (queue.isEmpty()) continue;
                    value = queue.poll();
                }

                if (value.index() > finish) continue;
                processing.getAndIncrement();
                value.value().accept(value.index());
                processing.getAndDecrement();
            }
        }
    }

    private void processValue(Element.Value<?> element, Sequence.Inserter resultInserter) {
        Object container;
        synchronized (containers) {
            container = containers.remove(element.index() - 1);
        }
        if (container == null) {
            container = gatherer.container();
        }

        try {
            if (gatherer.integrate(container, element.index(), element.value(), o -> {
                resultInserter.add(o);
            })) {
                if (element.index() < finish) {
                    finish = Math.min(finish, element.index());
                    synchronized (queue) {
                        queue.add(new Element.Value<>(finish, this::processFinish));
                    }
                }
            }
        } catch (Throwable e) {
            if (element.index() < finish) {
                finish = Math.min(finish, element.index());
                synchronized (queue) {
                    queue.add(new Element.Value<>(finish, this::processFinish));
                }
            }
        } finally {
            resultInserter.release();

            synchronized (containers) {
                if (finish >= element.index() && containers.containsKey(element.index() - 1)) {
                    container = gatherer.combine(containers.remove(element.index() - 1), container);
                }
                containers.put(element.index(), container);
            }
        }

        synchronized (processingIntermediateResults) {
            if (processingIntermediateResults.get()) {
                return;
            }
            processingIntermediateResults.set(true);
        }

        for (Object o : results) {
            if (index.get() > finish) continue;
            next.consume(new Element.Value<>(index.getAndIncrement(), o));
        }

        synchronized (containers) {
            List<Long> indices = containers.keySet()
                    .stream()
                    .sorted()
                    .filter(l -> l < element.index())
                    .collect(Collectors.toList());

            for (int i = 0; i < indices.size() - 1; i++) {
                long i1 = indices.get(i);
                long i2 = indices.get(i + 1);
                Object c1 = containers.remove(i1);
                Object c2 = containers.remove(i2);
                Object cr = gatherer.combine(c1, c2);
                containers.put(Math.max(i1, i2), cr);
            }
        }

        synchronized (processingIntermediateResults) {
            processingIntermediateResults.set(false);
        }
    }

    private void processFinish(long finishIndex) {
        while (processing.get() > 1) {
            if (finishIndex > finish) return;
            Thread.yield();
        }

        // Finish everything left in results!
        processingIntermediateResults.set(true);
        for (Object o : results) {
            if (index.get() > finish) continue;
            next.consume(new Element.Value<>(index.getAndIncrement(), o));
        }

        // Combining all containers to create a single result container!
        Queue<Element.Value<Object>> priorityQueue = new PriorityQueue<>(Comparator.comparingLong(Element.Value::index));
        containers.forEach((aLong, o) -> {
            if (aLong > finish) return;
            priorityQueue.add(new Element.Value<>(1L, o));
        });

        while (priorityQueue.size() > 1) {
            Element.Value<Object> first = priorityQueue.poll();
            Element.Value<Object> second = priorityQueue.poll();

            Object combined = gatherer.combine(first.value(), second.value());

            priorityQueue.add(new Element.Value<>(first.index() + second.index(), combined));
        }

        // Finishing the gatherer
        Object container = priorityQueue.poll().value();
        try {
            gatherer.finish(container, o -> {
                next.consume(new Element.Value<>(index.getAndIncrement(), o));
            });
            next.consume(new Element.Finish());
        } catch (FinishException e) {
            // Ignore
        }
    }
}
