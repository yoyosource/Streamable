package de.yoyosource.streamable3.internal;

import de.yoyosource.streamable3.StreamableCollector;
import de.yoyosource.streamable3.StreamableGatherer;
import de.yoyosource.streamable3.internal.finish.Finish;
import de.yoyosource.streamable3.internal.finish.SequentialFinish;
import de.yoyosource.streamable3.internal.root.Root;
import de.yoyosource.streamable3.internal.step.FlattenStep;
import de.yoyosource.streamable3.internal.step.ParallelStep;
import de.yoyosource.streamable3.internal.step.SequentialStep;
import de.yoyosource.streamable3.internal.step.Step;

import java.lang.reflect.Field;

public abstract class StreamableSupplier {

    protected volatile Root root = null;
    protected volatile StreamableConsumer next = null;

    public final <T, A, B> Step setNext(int maxParallelTasks, StreamableGatherer<T, A, B> gatherer) {
        if (this.next != null) {
            throw new IllegalStateException("Cannot add more than one next steps");
        }

        Step step;
        if (gatherer == null) {
            step = new FlattenStep();
        } else if (maxParallelTasks == 1) {
            step = new SequentialStep(gatherer);
        } else {
            step = new ParallelStep(gatherer, maxParallelTasks);
        }
        step.root = root;
        next = step;
        return step;
    }

    public final <T, A, R> Finish setNext(int maxParallelTasks, StreamableCollector<T, A, R> collector) {
        if (this.next != null) {
            throw new IllegalStateException("Cannot add more than one next steps");
        }

        if (collector == null) {
            throw new IllegalArgumentException("Collector must not be null!");
        }
        Finish toEvaluate;
        if (maxParallelTasks == 1) {
            toEvaluate = new SequentialFinish(collector);
            this.next = toEvaluate;
        } else {
            this.next = new ParallelStep(collector.toGatherer(), maxParallelTasks);
            toEvaluate = new SequentialFinish(new StreamableCollector.First());
            ((Step) this.next).next = toEvaluate;
        }
        root.evaluate();
        try {
            Field field = Finish.class.getDeclaredField("root");
            field.setAccessible(true);
            field.set(toEvaluate, root);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Ignore
        }
        return toEvaluate;
    }
}
