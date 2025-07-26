package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.FunctionWithException;
import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.Try;

import java.util.function.Consumer;

import static de.yoyosource.streamable.streams.JavaStream.JavaStream;

public interface TryedStream<T, E extends Throwable> extends Streamable<TryedStream<T, E>, Try<T, E>> {

    /**
     * Returns a {@code Class} instance with the generic type of {@code TryedStream} for {@link #as(Class)} method.
     *
     * @param <T> the type of elements inside the {@code TryedStream}
     * @param <E> the type of exception inside the {@code TryedStream}
     * @return the type for {@link #as(Class)}
     */
    static <T, E extends Throwable> Class<TryedStream<T, E>> TryedStream() {
        return (Class<TryedStream<T, E>>) (Class) TryedStream.class;
    }

    /**
     * Returns a {@code Class} instance with the generic type of {@code TryedStream} for {@link #as(Class)} method.
     *
     * @param <T> the type of elements inside the {@code TryedStream}
     * @param <E> the type of exception inside the {@code TryedStream}
     * @param element the type {@code T} should be
     * @param exception the type {@code E} should be
     * @return the type for {@link #as(Class)}
     */
    static <T, E extends Throwable> Class<TryedStream<T, E>> TryedStream(Class<T> element, Class<E> exception) {
        return (Class<TryedStream<T, E>>) (Class) TryedStream.class;
    }

    abstract class Option<T, E extends Throwable, R> {
        protected abstract boolean check(Try<T, E> toCheck);
        protected abstract R unwrap(Try<T, E> toUnwrap);

        private Option() {
        }

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
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, Try<T, E> element, Consumer<? super Try<T, E>> next) {
                if (option.check(element)) next.accept(element);
                return false;
            }

            @Override
            public void finish(Consumer<? super Try<T, E>> next) {
            }
        });
    }

    default <U> JavaStream<U> unwrap(Option<T, E, U> option) {
        return gather(new StreamableGatherer.Simple<Try<T, E>, U>() {
            @Override
            public boolean integrate(long index, Try<T, E> element, Consumer<? super U> next) {
                next.accept(option.unwrap(element));
                return false;
            }

            @Override
            public void finish(Consumer<? super U> next) {
            }
        }).as(JavaStream());
    }

    default <U> JavaStream<U> keepAndUnwrap(Option<T, E, U> option) {
        return keep(option).unwrap(option);
    }

    default <U> TryedStream<T, E> peek(Option<T, E, U> option, Consumer<U> consumer) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, Try<T, E> element, Consumer<? super Try<T, E>> next) {
                if (option.check(element)) consumer.accept(option.unwrap(element));
                next.accept(element);
                return false;
            }

            @Override
            public void finish(Consumer<? super Try<T, E>> next) {
            }
        });
    }

    default <R> TryedStream<R, E> tryIt(FunctionWithException<T, R, E> functionWithException) {
        return tryIt(functionWithException, false);
    }

    default <R> TryedStream<R, E> tryIt(FunctionWithException<T, R, E> functionWithException, boolean endOnException) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, Try<T, E> element, Consumer<? super Try<R, E>> next) {
                if (element.successful()) {
                    try {
                        next.accept(Try.Success(functionWithException.apply(element.getSuccess())));
                    } catch (Throwable e) {
                        if (endOnException) {
                            return true;
                        } else {
                            next.accept(Try.Failure((E) e));
                        }
                    }
                } else {
                    if (endOnException) {
                        return true;
                    }
                    next.accept(Try.Failure(element.getFailure()));
                }
                return false;
            }

            @Override
            public void finish(Consumer<? super Try<R, E>> next) {
            }
        });
    }
}
