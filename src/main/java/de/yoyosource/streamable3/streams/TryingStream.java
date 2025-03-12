package de.yoyosource.streamable3.streams;

import de.yoyosource.streamable3.FunctionWithException;
import de.yoyosource.streamable3.Try;
import de.yoyosource.streamable3.Streamable;
import de.yoyosource.streamable3.StreamableGatherer;

import java.util.function.Consumer;

import static de.yoyosource.streamable3.streams.TryedStream.TryedStream;

public interface TryingStream<T> extends Streamable<TryingStream<T>, T> {

    static <T> Class<TryingStream<T>> TryingStream() {
        return (Class<TryingStream<T>>) (Class) TryingStream.class;
    }

    static <T> Class<TryingStream<T>> TryingStream(Class<T> clazz) {
        return (Class<TryingStream<T>>) (Class) TryingStream.class;
    }

    default <R, E extends Throwable> TryedStream<R, E> tryIt(FunctionWithException<T, R, E> functionWithException) {
        return gather(new StreamableGatherer.Simple<T, Try<R, E>>() {
            @Override
            public boolean integrate(long index, T element, Consumer<? super Try<R, E>> next) {
                try {
                    next.accept(Try.Success(functionWithException.apply(element)));
                } catch (Throwable e) {
                    next.accept(Try.Failure((E) e));
                }
                return false;
            }

            @Override
            public void finish(Consumer<? super Try<R, E>> next) {
            }
        }).as(TryedStream());
    }
}
