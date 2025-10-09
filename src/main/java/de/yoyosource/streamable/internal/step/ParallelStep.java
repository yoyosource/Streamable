package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.Evaluation;
import de.yoyosource.streamable.Ordering;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.ThreadManager;
import de.yoyosource.streamable.internal.ContainerManager;
import de.yoyosource.streamable.internal.Element;
import de.yoyosource.streamable.internal.FinishException;
import de.yoyosource.streamable.internal.sequence.OrderedSequence;
import de.yoyosource.streamable.internal.sequence.Sequence;
import de.yoyosource.streamable.internal.sequence.UnorderedSequence;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ParallelStep extends Step {

    private Sequence<Element<?>> results = null;
    private ContainerManager containers = null;
    private final int maxParallelTasks;

    public ParallelStep(StreamableGatherer streamableGatherer, int maxParallelTasks) {
        super(streamableGatherer);
        this.maxParallelTasks = maxParallelTasks;
    }

    @Override
    public Ordering ordering() {
        // This will never return sequential.
        // Since otherwise the SequentialStep should have been used!
        return gatherer.ordering();
    }

    @Override
    public Set<Evaluation> evaluation() {
        return StreamableGatherer.getEvaluation(gatherer);
    }

    public void setSequenceType(boolean ordered) {
        if (ordered) {
            results = new OrderedSequence<>();
        } else {
            results = new UnorderedSequence<>();
        }
    }

    public void setContainerManager(boolean greedy) {
        // this.containers = ContainerManager.get(gatherer, greedy);
        this.containers = new ContainerManager.Base(gatherer);
    }

    private final Object insertLock = new Object();
    private long insertFinished = Integer.MAX_VALUE;
    private long insertIndex = 0;
    private Queue<Element<Runnable>> tasks = new LinkedList<>();

    private ThreadManager.QueueKey queueKey = null;

    private void startWorker() {
        if (queueKey != null) return;
        AtomicLong processing = new AtomicLong();
        queueKey = ThreadManager.queueToCurrent(() -> {
            Element<Runnable> element;
            synchronized (insertLock) {
                if (tasks.isEmpty()) return;
                element = tasks.poll();
            }
            if (element == null) return;

            if (element instanceof Element.Value<Runnable>(long index, Runnable value)) {
                processing.incrementAndGet();
                try {
                    if (index > insertFinished) return;
                    value.run();
                } finally {
                    processing.decrementAndGet();
                }
            } else {
                if (processing.get() > 0) {
                    synchronized (insertLock) {
                        tasks.add(element);
                    }
                } else {
                    processFinish();
                }
            }
        }, maxParallelTasks);

        synchronized (insertLock) {
            tasks.add(new Element.Value<>(-1, this::processResults));
        }
    }

    @Override
    public void consume(long index, Object value) {
        synchronized (insertLock) {
            if (index > insertFinished) throw FinishException.INSTANCE;
            if (insertIndex != index) throw new IllegalStateException("Out of order: " + insertIndex + " != " + index);
            insertIndex++;

            Sequence.Inserter inserter = results.inserter();
            tasks.add(new Element.Value<>(index, () -> processValue(index, value, inserter)));
            startWorker();
        }
    }

    @Override
    public void finish() {
        synchronized (insertLock) {
            if (insertIndex > insertFinished) throw FinishException.INSTANCE;
            insertFinished = insertIndex;

            tasks.add(Element.Finish.getInstance());
            startWorker();
        }
    }

    private final Lock processingLock = new ReentrantLock(true);

    private void processValue(long index, Object value, Sequence.Inserter resultInserter) {
        Object container = containers.remove(index - 1);
        if (container == null) {
            container = gatherer.container();
        }

        try {
            if (!gatherer.integrate(container, index, value, resultInserter::add)) {
                throw FinishException.INSTANCE;
            }
        } catch (FinishException e) {
            synchronized (insertLock) {
                insertFinished = Math.min(insertFinished, index);
                tasks.add(Element.Finish.getInstance());
                resultInserter.cutShort();
            }
        } catch (Throwable e) {
            root.setError(e);
            queueKey.dequeue();
            return;
        }

        resultInserter.release();
        containers.set(index, container);
    }

    private void processResults() {
        if (!processingLock.tryLock()) {
            synchronized (insertLock) {
                tasks.add(new Element.Value<>(-1, this::processResults));
            }
            return;
        }

        try {
            evaluateResults();
            synchronized (insertLock) {
                tasks.add(new Element.Value<>(-1, this::processResults));
            }
        } catch (Throwable e) {
            root.setError(e);
            queueKey.dequeue();
        } finally {
            processingLock.unlock();
        }
    }

    private void processFinish() {
        queueKey.dequeue();
        processingLock.lock();

        try {
            evaluateResults();
            containers.combine(insertFinished);
            evaluateLastContainer();
        } catch (Throwable e) {
            root.setError(e);
            return;
        }
        try {
            next.finish();
        } catch (FinishException e) {
            // Ignore
        }
    }

    private long evaluateIndex = 0;
    private long evaluateFinish = Long.MAX_VALUE;

    private void evaluateResults() {
        for (Object o : results) {
            if (evaluateIndex > evaluateFinish) continue;
            try {
                next.consume(evaluateIndex++, o);
            } catch (FinishException e) {
                evaluateFinish = Math.min(evaluateIndex, evaluateFinish);
                results.finish();
            }
        }
    }

    private void evaluateLastContainer() {
        Object container;
        synchronized (containers) {
            if (containers.isEmpty()) {
                container = gatherer.container();
            } else if (containers.size() == 1) {
                container = containers.getAny();
            } else {
                container = containers.get(insertFinished);
            }
        }
        try {
            gatherer.finish(container, o -> {
                next.consume(evaluateIndex++, o);
            });
        } catch (FinishException e) {
            // Ignore
        }
    }
}
