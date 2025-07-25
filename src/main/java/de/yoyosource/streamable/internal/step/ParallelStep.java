package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.Ordering;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.ThreadManager;
import de.yoyosource.streamable.internal.Element;
import de.yoyosource.streamable.internal.FinishException;
import de.yoyosource.streamable.internal.sequence.Sequence;
import de.yoyosource.streamable.internal.sequence.OrderedSequence;
import de.yoyosource.streamable.internal.sequence.UnorderedSequence;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ParallelStep extends Step {

    private final ThreadManager.QueueKey queueKey;
    private AtomicLong processing = new AtomicLong();

    private final AtomicBoolean processingIntermediateResults = new AtomicBoolean();

    private final Queue<Element.Value<Consumer<Long>>> queue = new LinkedList<>();

    private long finish = Integer.MAX_VALUE;
    private long counter = 0;

    private final Map<Long, Object> containers = new HashMap<>();

    private final AtomicLong index = new AtomicLong();
    private Sequence results = null;

    public ParallelStep(StreamableGatherer streamableGatherer, int maxParallelTasks) {
        super(streamableGatherer);
        if (maxParallelTasks == 0) {
            throw new IllegalArgumentException("maxParallelTasks must be greater than 0");
        }
        queueKey = ThreadManager.queueToCurrent(() -> {
            Element.Value<Consumer<Long>> value;
            synchronized (queue) {
                if (queue.isEmpty()) return;
                value = queue.poll();
            }

            if (value.index() > finish) return;
            processing.getAndIncrement();
            value.value().accept(value.index());
            processing.getAndDecrement();
        }, maxParallelTasks);
    }

    @Override
    public Ordering ordering() {
        // This will never return sequential.
        // Since otherwise the SequentialStep should have been used!
        return gatherer.ordering();
    }

    public void setSequenceType(boolean ordered) {
        if (ordered) {
            results = new OrderedSequence();
        } else {
            results = new UnorderedSequence();
        }
    }

    @Override
    public void consume(long index, Object value) {
        if (counter > finish) throw FinishException.INSTANCE;
        Sequence.Inserter result = results.inserter();
        synchronized (queue) {
            queue.add(new Element.Value<>(counter++, __ -> processValue(index, value, result)));
        }
    }

    @Override
    public void finish() {
        if (counter > finish) throw FinishException.INSTANCE;
        synchronized (queue) {
            finish = Math.min(finish, counter);
            queue.add(new Element.Value<>(counter++, this::processFinish));
        }
    }

    private void processValue(long index, Object value, Sequence.Inserter resultInserter) {
        Object container;
        synchronized (containers) {
            container = containers.remove(index - 1);
        }
        if (container == null) {
            container = gatherer.container();
        }

        try {
            if (gatherer.integrate(container, index, value, o -> {
                resultInserter.add(o);
            })) {
                if (index < finish) {
                    finish = Math.min(finish, index);
                    synchronized (queue) {
                        queue.add(new Element.Value<>(finish, this::processFinish));
                    }
                }
            }
        } catch (Throwable e) {
            if (index < finish) {
                finish = Math.min(finish, index);
                synchronized (queue) {
                    queue.add(new Element.Value<>(finish, this::processFinish));
                }
            }
        } finally {
            resultInserter.release();

            synchronized (containers) {
                if (finish >= index && containers.containsKey(index - 1)) {
                    container = gatherer.combine(containers.remove(index - 1), container);
                }
                containers.put(index, container);
            }
        }

        synchronized (processingIntermediateResults) {
            if (processingIntermediateResults.get()) {
                return;
            }
            processingIntermediateResults.set(true);
        }

        for (Object o : results) {
            if (this.index.get() > finish) continue;
            try {
                next.consume(this.index.getAndIncrement(), o);
            } catch (FinishException e) {
                finish = Math.min(finish, this.index.get());
                break;
            }
        }

        synchronized (containers) {
            List<Long> indices = containers.keySet()
                    .stream()
                    .sorted()
                    .filter(l -> l < index)
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
        queueKey.dequeue();
        while (processing.get() > 1) {
            if (finishIndex > finish) return;
            Thread.yield();
        }

        // Finish everything left in results!
        processingIntermediateResults.set(true);
        for (Object o : results) {
            if (index.get() > finish) continue;
            next.consume(index.getAndIncrement(), o);
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
                next.consume(index.getAndIncrement(), o);
            });
            next.finish();
        } catch (FinishException e) {
            // Ignore
        }
    }
}
