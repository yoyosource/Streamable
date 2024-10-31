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

    default <R, E extends Throwable> TryedStream<R, E> tryIt(FunctionWithException<T, R, E> functionWithException) {
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
        }).as(TryedStream.type());
    }
}
