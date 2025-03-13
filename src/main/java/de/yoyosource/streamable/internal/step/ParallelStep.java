package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.internal.FinishException;
import de.yoyosource.streamable.internal.Sequence;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.Element;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ParallelStep extends Step {

    private AtomicLong processing = new AtomicLong();

    private AtomicBoolean processingIntermediateResults = new AtomicBoolean();

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
                queue.add(new Element.Value<>(counter++, new Finisher()));
            }
        }
    }

    private final class Finisher implements Consumer<Long> {

        @Override
        public void accept(Long aLong) {
            processFinish(aLong);
        }

        @Override
        public String toString() {
            return "Finisher{}";
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
                        queue.add(new Element.Value<>(finish, new Finisher()));
                    }
                }
            }
        } catch (Throwable e) {
            if (element.index() < finish) {
                finish = Math.min(finish, element.index());
                synchronized (queue) {
                    queue.add(new Element.Value<>(finish, new Finisher()));
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

        if (!processingIntermediateResults.compareAndSet(false, true)) {
            for (Object o : results) {
                if (index.get() > finish) continue;
                next.consume(new Element.Value<>(index.getAndIncrement(), o));
            }
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
