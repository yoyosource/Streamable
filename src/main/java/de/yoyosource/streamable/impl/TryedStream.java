package de.yoyosource.streamable.impl;

import de.yoyosource.streamable.FunctionWithException;
import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.Try;

import java.util.function.Consumer;

public interface TryedStream<T, E extends Throwable> extends Streamable<Try<T, E>> {

    static <T, E extends Throwable> Class<TryedStream<T, E>> type() {
        return (Class<TryedStream<T, E>>) (Class) TryedStream.class;
    }

    abstract class Option<T, E extends Throwable, R> {
        protected abstract boolean check(Try<T, E> toCheck);
        protected abstract R unwrap(Try<T, E> toUnwrap);

        private static final Option<?, ?, ?> SUCCESSFUL = new Option<>() {
            @Override
            public boolean check(Try<Object, Throwable> toCheck) {
                return toCheck.successful();
            }

            @Override
            public Object unwrap(Try<Object, Throwable> toUnwrap) {
                return toUnwrap.getSuccess();
            }
        };

        private static final Option<?, ?, ?> FAILED = new Option<>() {
            @Override
            public boolean check(Try<Object, Throwable> toCheck) {
                return toCheck.failed();
            }

            @Override
            public Object unwrap(Try<Object, Throwable> toUnwrap) {
                return toUnwrap.getFailure();
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <T, E extends Throwable> Option<T, E, T> successful() {
        return (Option<T, E, T>) Option.SUCCESSFUL;
    }

    @SuppressWarnings("unchecked")
    static <T, E extends Throwable> Option<T, E, E> failed() {
        return (Option<T, E, E>) Option.FAILED;
    }

    default TryedStream<T, E> keep(Option<T, E, ?> option) {
        return gather(new StreamableGatherer<>() {
            @Override
            public boolean apply(Try<T, E> input, Consumer<Try<T, E>> next) {
                if (option.check(input)) next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<Try<T, E>> next) {
            }
        });
    }

    default <U> Streamable<U> unwrap(Option<T, E, U> option) {
        return gather(new StreamableGatherer<Try<T, E>, U>() {
            @Override
            public boolean apply(Try<T, E> input, Consumer<U> next) {
                next.accept(option.unwrap(input));
                return false;
            }

            @Override
            public void finish(Consumer<U> next) {
            }
        }).as(Streamable.type());
    }

    default <U> Streamable<U> keepAndUnwrap(Option<T, E, U> option) {
        return keep(option).unwrap(option);
    }

    default <U> TryedStream<T, E> peek(Option<T, E, U> option, Consumer<U> consumer) {
        return gather(new StreamableGatherer<>() {
            @Override
            public boolean apply(Try<T, E> input, Consumer<Try<T, E>> next) {
                if (option.check(input)) consumer.accept(option.unwrap(input));
                next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<Try<T, E>> next) {
            }
        });
    }

    default <R> TryedStream<R, E> tryIt(FunctionWithException<T, R, E> functionWithException) {
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
