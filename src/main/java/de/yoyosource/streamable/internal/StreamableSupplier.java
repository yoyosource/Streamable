package de.yoyosource.streamable.internal;

import de.yoyosource.streamable.Evaluation;
import de.yoyosource.streamable.Ordering;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.finish.Finish;
import de.yoyosource.streamable.internal.finish.GreedySequentialFinish;
import de.yoyosource.streamable.internal.finish.SequentialFinish;
import de.yoyosource.streamable.internal.root.Root;
import de.yoyosource.streamable.internal.step.GreedyParallelStep;
import de.yoyosource.streamable.internal.step.GreedySequentialStep;
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

        Set<Evaluation> evaluation = gatherer.evaluation();
        if (evaluation.contains(Evaluation.SEQUENTIAL)) {
            maxParallelTasks = 1;
        }

        boolean greedy = evaluation.contains(Evaluation.GREEDY);
        if (maxParallelTasks == 1) {
            if (greedy) {
                return setNext(new GreedySequentialStep(gatherer));
            } else {
                return setNext(new SequentialStep(gatherer));
            }
        } else {
            if (greedy) {
                return setNext(new GreedyParallelStep(gatherer, maxParallelTasks));
            } else {
                return setNext(new ParallelStep(gatherer, maxParallelTasks));
            }
        }
    }

    public final <T, A, R> Finish setNext(int maxParallelTasks, StreamableCollector<T, A, R> collector) {
        if (this.next != null) {
            throw new IllegalStateException("Cannot add more than one next steps");
        }
        if (collector == null) {
            throw new IllegalArgumentException("Collector must not be null!");
        }

        Set<Evaluation> evaluation = collector.evaluation();
        if (evaluation.contains(Evaluation.SEQUENTIAL)) {
            maxParallelTasks = 1;
        }

        boolean greedy = evaluation.contains(Evaluation.GREEDY);
        if (maxParallelTasks == 1) {
            if (greedy) {
                return setNext(new GreedySequentialFinish(collector));
            } else {
                return setNext(new SequentialFinish(collector));
            }
        } else {
            Step step;
            StreamableGatherer<T, A, R> gatherer = new StreamableGatherer<T, A, R>() {
                @Override
                public Ordering ordering() {
                    return collector.ordering();
                }

                @Override
                public Set<Evaluation> evaluation() {
                    return collector.evaluation();
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
            };
            if (greedy) {
                step = setNext(new GreedyParallelStep(gatherer, maxParallelTasks));
            } else {
                step = setNext(new ParallelStep(gatherer, maxParallelTasks));
            }
            StreamableCollector.Simple<R, R> finalCollector = new StreamableCollector.Simple<R, R>() {
                @Override
                public Ordering ordering() {
                    return collector.ordering();
                }

                @Override
                public Set<Evaluation> evaluation() {
                    return Evaluation.sequential;
                }

                private R element = null;

                @Override
                public boolean accumulate(long index, R element) {
                    this.element = element;
                    return true;
                }

                @Override
                public R finish() {
                    return element;
                }
            };
            if (greedy) {
                return step.setNext(new GreedySequentialFinish(finalCollector));
            } else {
                return step.setNext(new SequentialFinish(finalCollector));
            }
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
