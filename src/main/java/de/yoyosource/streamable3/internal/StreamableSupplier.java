package de.yoyosource.streamable3.internal;

import de.yoyosource.streamable3.StreamableCollector;
import de.yoyosource.streamable3.StreamableGatherer;
import de.yoyosource.streamable3.internal.finish.Finish;
import de.yoyosource.streamable3.internal.finish.ParallelFinish;
import de.yoyosource.streamable3.internal.finish.SequentialFinish;
import de.yoyosource.streamable3.internal.root.Root;
import de.yoyosource.streamable3.internal.step.FixedParallelStep;
import de.yoyosource.streamable3.internal.step.ParallelStep;
import de.yoyosource.streamable3.internal.step.SequentialStep;
import de.yoyosource.streamable3.internal.step.Step;

public abstract class StreamableSupplier {

    protected volatile Root root = null;
    protected volatile StreamableConsumer next = null;

    public final <T, A, B> Step setNext(int maxParallelTasks, StreamableGatherer<T, A, B> gatherer) {
        if (gatherer == null) {
            throw new IllegalArgumentException("Gatherer must not be null!");
        }
        Step step;
        if (maxParallelTasks == 1) {
            step = new SequentialStep(gatherer);
        } else {
            step = new FixedParallelStep(gatherer, maxParallelTasks);
        }
        step.root = root;
        next = step;
        return step;
    }

    public final <T, A, R> R setNext(int maxParallelTasks, StreamableCollector<T, A, R> collector) {
        if (collector == null) {
            throw new IllegalArgumentException("Collector must not be null!");
        }
        if (maxParallelTasks == 1) {
            this.next = new SequentialFinish(collector);
        } else {
            this.next = new ParallelFinish(collector, maxParallelTasks);
        }
        root.evaluate();
        return (R) ((Finish) this.next).waitForResult();
    }
}
