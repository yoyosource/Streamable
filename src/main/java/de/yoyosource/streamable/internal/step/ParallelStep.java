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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ParallelStep extends Step {

    private Sequence<Element<?>> results = null;
    private final int maxParallelTasks;
    private final boolean greedy;

    public ParallelStep(StreamableGatherer streamableGatherer, int maxParallelTasks) {
        super(streamableGatherer);
        if (maxParallelTasks <= 0) {
            throw new IllegalArgumentException("maxParallelTasks must be greater than 0");
        }
        this.maxParallelTasks = maxParallelTasks;
        this.greedy = streamableGatherer.evaluation().contains(Evaluation.GREEDY);
    }

    @Override
    public Ordering ordering() {
        // This will never return sequential.
        // Since otherwise the SequentialStep should have been used!
        return gatherer.ordering();
    }

    @Override
    public Set<Evaluation> evaluation() {
        return gatherer.evaluation();
    }

    public void setSequenceType(boolean ordered) {
        if (ordered) {
            results = new OrderedSequence<>();
        } else {
            results = new UnorderedSequence<>();
        }
    }

    public void setContainerManager(boolean greedy) {
        this.containers = ContainerManager.get(gatherer, greedy);
    }

    private long insertIndex = 0;
    private long insertFinish = Long.MAX_VALUE;
    private final Lock insertLock = new ReentrantLock(true);
    private final Queue<Element.Value<Runnable>> queue = new LinkedList<>();

    private final AtomicBoolean queueKeyStarted = new AtomicBoolean(false);
    private ThreadManager.QueueKey queueKey = null;
    private AtomicLong processing = new AtomicLong();

    private void startWorker() {
        if (queueKeyStarted.getAndSet(true)) return;
        queueKey = ThreadManager.queueToCurrent(() -> {
            Element.Value<Runnable> value;
            insertLock.lock();
            if (queue.isEmpty()) {
                insertLock.unlock();
                return;
            }
            value = queue.poll();
            if (value.index() > insertFinish) {
                insertLock.unlock();
                return;
            }
            insertLock.unlock();

            processing.incrementAndGet();
            value.value().run();
            processing.decrementAndGet();
        }, maxParallelTasks);
    }

    @Override
    public void consume(long index, Object value) {
        if (insertIndex > insertFinish) throw FinishException.INSTANCE;
        Sequence.Inserter result = results.inserter();
        insertLock.lock();
        queue.add(new Element.Value<>(insertIndex++, () -> processValue(index, value, result)));
        insertLock.unlock();
        startWorker();
    }

    @Override
    public void finish() {
        if (insertIndex > insertFinish) throw FinishException.INSTANCE;
        insertLock.lock();
        insertFinish = Math.min(insertIndex, insertFinish);
        queue.add(new Element.Value<>(insertIndex++, this::processFinish));
        insertLock.unlock();
        startWorker();
    }

    private ContainerManager containers = null;
    private final Lock processingLock = new ReentrantLock(true);

    private void processValue(long index, Object value, Sequence.Inserter resultInserter) {
        Object container = containers.remove(index - 1);
        if (container == null) {
            container = gatherer.container();
        }

        try {
            if (greedy) {
                gatherer.integrate(container, index, value, resultInserter::add);
            } else {
                if (gatherer.integrate(container, index, value, resultInserter::add)) {
                    insertLock.lock();
                    insertFinish = Math.min(insertFinish, index);
                    insertLock.unlock();
                }
            }
        } catch (Throwable e) {
            insertLock.lock();
            insertFinish = Math.min(insertFinish, index);
            insertLock.unlock();
        }

        resultInserter.release();
        containers.set(index, container);

        if (!processingLock.tryLock()) {
            return;
        }

        try {
            evaluateResults();
            containers.combine(index);
        } finally {
            processingLock.unlock();
        }
    }

    private void processFinish() {
        if (!queue.isEmpty() || processing.get() > 1) {
            insertLock.lock();
            queue.add(new Element.Value<>(insertFinish, this::processFinish));
            insertLock.unlock();
            return;
        }

        queueKey.dequeue();
        processingLock.lock();
        try {
            evaluateResults();
            containers.combine(insertFinish);
            evaluateLastContainer();
        } finally {
            processingLock.unlock();
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
                throw new IllegalStateException();
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
