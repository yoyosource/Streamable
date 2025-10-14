package de.yoyosource.streamable.internal;

import de.yoyosource.streamable.Evaluation;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.finish.Finish;
import de.yoyosource.streamable.internal.finish.SequentialFinish;
import de.yoyosource.streamable.internal.root.Root;
import de.yoyosource.streamable.internal.step.ParallelStep;
import de.yoyosource.streamable.internal.step.SequentialStep;
import de.yoyosource.streamable.internal.step.Step;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.function.Consumer;

public abstract class StreamableSupplier {

    protected volatile Root root = null;

    @Getter
    protected volatile StreamableConsumer next = null;

    public final <T, A, B> Step setNext(int maxParallelTasks, StreamableGatherer<T, A, B> gatherer) {
        if (this.next != null) {
            throw new IllegalStateException("Cannot add more than one next steps");
        }
        if (gatherer == null) {
            throw new IllegalArgumentException("Gatherer must not be null!");
        }

        Evaluation.EvaluationValidSet evaluation = StreamableGatherer.getEvaluation(gatherer);
        if (evaluation.contains(Evaluation.SEQUENTIAL)) {
            maxParallelTasks = 1;
        }

        if (maxParallelTasks == 1) {
            return setNext(new SequentialStep(gatherer));
        } else {
            return setNext(new ParallelStep(gatherer, maxParallelTasks));
        }
    }

    public final <T, A, R> Finish setNext(int maxParallelTasks, StreamableCollector<T, A, R> collector) {
        if (this.next != null) {
            throw new IllegalStateException("Cannot add more than one next steps");
        }
        if (collector == null) {
            throw new IllegalArgumentException("Collector must not be null!");
        }

        Evaluation.EvaluationValidSet evaluation = StreamableCollector.getEvaluation(collector);
        if (evaluation.contains(Evaluation.SEQUENTIAL)) {
            maxParallelTasks = 1;
        }

        if (maxParallelTasks == 1) {
            return setNext(new SequentialFinish(collector));
        } else {
            return setNext(new ParallelStep(new StreamableGatherer<T, A, R>() {
                @Override
                public Evaluation.EvaluationValidSet evaluation() {
                    return StreamableCollector.getEvaluation(collector);
                }

                @Override
                public A container() {
                    return collector.container();
                }

                @Override
                public boolean integrate(A container, long index, T element, Consumer<? super R> next) {
                    return collector.accumulate(container, index, element);
                }

                @Override
                public A combine(A firstContainer, A secondContainer) {
                    return collector.combine(firstContainer, secondContainer);
                }

                @Override
                public void finish(A container, Consumer<? super R> next) {
                    next.accept(collector.finish(container));
                }
            }, maxParallelTasks)).setNext(new SequentialFinish(new StreamableCollector.Simple<R, R>() {
                @Override
                public Evaluation.EvaluationValidSet evaluation() {
                    return collector.evaluation().with(Evaluation.SEQUENTIAL, Evaluation.NO_CONTAINER);
                }

                private R element = null;

                @Override
                public boolean accumulate(long index, R element) {
                    this.element = element;
                    return false;
                }

                @Override
                public R finish() {
                    return element;
                }
            }));
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
