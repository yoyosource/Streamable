package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.sequence.Sequence;

public class GreedyParallelStep extends ParallelStep {

    public GreedyParallelStep(StreamableGatherer streamableGatherer, int maxParallelTasks) {
        super(streamableGatherer, maxParallelTasks);
    }

    @Override
    protected void processValue(long index, Object value, Sequence.Inserter resultInserter) {
        Object container = containers.remove(index - 1);
        if (container == null) {
            container = gatherer.container();
        }

        try {
            gatherer.integrate(container, index, value, resultInserter::add);
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
}
