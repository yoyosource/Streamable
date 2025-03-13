package de.yoyosource.streamable.internal;

import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.finish.Finish;
import de.yoyosource.streamable.internal.finish.SequentialFinish;
import de.yoyosource.streamable.internal.root.Root;
import de.yoyosource.streamable.internal.step.FlattenStep;
import de.yoyosource.streamable.internal.step.ParallelStep;
import de.yoyosource.streamable.internal.step.SequentialStep;
import de.yoyosource.streamable.internal.step.Step;
import lombok.Getter;

import java.lang.reflect.Field;

public abstract class StreamableSupplier {

    protected volatile Root root = null;

    @Getter
    protected volatile StreamableConsumer next = null;

    public final <T, A, B> Step setNext(int maxParallelTasks, StreamableGatherer<T, A, B> gatherer) {
        if (this.next != null) {
            throw new IllegalStateException("Cannot add more than one next steps");
        }

        if (gatherer == null) {
            return setNext(new FlattenStep());
        } else if (maxParallelTasks == 1) {
            return setNext(new SequentialStep(gatherer));
        } else {
            return new ParallelStep(gatherer, maxParallelTasks);
        }
    }

    public final <T, A, R> Finish setNext(int maxParallelTasks, StreamableCollector<T, A, R> collector) {
        if (this.next != null) {
            throw new IllegalStateException("Cannot add more than one next steps");
        }

        if (collector == null) {
            throw new IllegalArgumentException("Collector must not be null!");
        }
        if (maxParallelTasks == 1) {
            return setNext(new SequentialFinish(collector));
        } else {
            return setNext(new ParallelStep(collector.toGatherer(), maxParallelTasks))
                    .setNext(new SequentialFinish(new StreamableCollector.First()));
        }
    }

    public final <T extends StreamableConsumer> T setNext(T streamableConsumer) {
        this.next = streamableConsumer;
        if (streamableConsumer instanceof StreamableSupplier streamableSupplier) {
            streamableSupplier.root = root;
        } else if (streamableConsumer instanceof Finish finish) {
            try {
                Field field = Finish.class.getDeclaredField("root");
                field.setAccessible(true);
                field.set(finish, root);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Ignore
            }
        }
        return streamableConsumer;
    }
}
