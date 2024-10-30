package de.yoyosource.streamable.impl;

import de.yoyosource.streamable.FunctionWithException;
import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.Try;

import java.util.function.Consumer;

public interface TryingStream<T> extends Streamable<T> {

    static <T> Class<TryingStream<T>> type() {
        return (Class<TryingStream<T>>) (Class) TryingStream.class;
    }

    default <R, E extends Throwable> TryStream<R, E> tryIt(FunctionWithException<T, R, E> functionWithException) {
        return gather(new StreamableGatherer<T, Try<R, E>>() {
            @Override
            public boolean apply(T input, Consumer<Try<R, E>> next) {
                try {
                    next.accept(Try.Success(functionWithException.apply(input)));
                } catch (Throwable e) {
                    next.accept(Try.Failure((E) e));
                }
                return false;
            }

            @Override
            public void finish(Consumer<Try<R, E>> next) {
            }
        }).as(TryStream.type());
    }

    interface TryStream<T, E extends Throwable> extends Streamable<Try<T, E>> {

        static <T, E extends Throwable> Class<TryStream<T, E>> type() {
            return (Class<TryStream<T, E>>) (Class) TryStream.class;
        }

        default TryStream<T, E> keepSuccessful() {
            return gather(new StreamableGatherer<>() {
                @Override
                public boolean apply(Try<T, E> input, Consumer<Try<T, E>> next) {
                    if (input.successful()) next.accept(input);
                    return false;
                }

                @Override
                public void finish(Consumer<Try<T, E>> next) {
                }
            });
        }

        default Streamable<T> keepSuccesssfulAndUnwrap() {
            return gather(new StreamableGatherer<>() {
                @Override
                public boolean apply(Try<T, E> input, Consumer<T> next) {
                    if (input.successful()) next.accept(input.getSuccess());
                    return false;
                }

                @Override
                public void finish(Consumer<T> next) {
                }
            });
        }

        default TryStream<T, E> peekSuccessful(Consumer<? super T> consumer) {
            return gather(new StreamableGatherer<>() {
                @Override
                public boolean apply(Try<T, E> input, Consumer<Try<T, E>> next) {
                    if (input.successful()) consumer.accept(input.getSuccess());
                    next.accept(input);
                    return false;
                }

                @Override
                public void finish(Consumer<Try<T, E>> next) {
                }
            });
        }

        default TryStream<T, E> keepFailed() {
            return gather(new StreamableGatherer<>() {
                @Override
                public boolean apply(Try<T, E> input, Consumer<Try<T, E>> next) {
                    if (input.failed()) next.accept(input);
                    return false;
                }

                @Override
                public void finish(Consumer<Try<T, E>> next) {
                }
            });
        }

        default Streamable<E> keepFailedAndUnwrap() {
            return gather(new StreamableGatherer<>() {
                @Override
                public boolean apply(Try<T, E> input, Consumer<E> next) {
                    if (input.failed()) next.accept(input.getFailure());
                    return false;
                }

                @Override
                public void finish(Consumer<E> next) {
                }
            });
        }

        default TryStream<T, E> peekFailed(Consumer<? super E> consumer) {
            return gather(new StreamableGatherer<>() {
                @Override
                public boolean apply(Try<T, E> input, Consumer<Try<T, E>> next) {
                    if (input.failed()) consumer.accept(input.getFailure());
                    next.accept(input);
                    return false;
                }

                @Override
                public void finish(Consumer<Try<T, E>> next) {
                }
            });
        }

        default <R> TryStream<R, E> tryIt(FunctionWithException<T, R, E> functionWithException) {
            return gather(new StreamableGatherer<>() {
                @Override
                public boolean apply(Try<T, E> input, Consumer<Try<R, E>> next) {
                    if (input.successful()) {
                        try {
                            next.accept(Try.Success(functionWithException.apply(input.getSuccess())));
                        } catch (Throwable e) {
                            next.accept(Try.Failure((E) e));
                        }
                    } else {
                        next.accept(Try.Failure(input.getFailure()));
                    }
                    return false;
                }

                @Override
                public void finish(Consumer<Try<R, E>> next) {
                }
            });
        }
    }
}
