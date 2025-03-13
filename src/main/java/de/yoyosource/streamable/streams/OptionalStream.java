package de.yoyosource.streamable.streams;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface OptionalStream<T> extends OptionalBaseStream<OptionalStream<T>, T> {

    static <T> Class<OptionalStream<T>> OptionalStream() {
        return (Class<OptionalStream<T>>) (Class) OptionalStream.class;
    }

    static <T> Class<OptionalStream<T>> OptionalStream(Class<T> clazz) {
        return (Class<OptionalStream<T>>) (Class) OptionalStream.class;
    }

    default <U> OptionalStream<U> map(Function<? super T, ? extends U> mapper) {
        return OptionalBaseStream.super._map(mapper);
    }

    default TryedStream<T, NoSuchElementException> get() {
        return as(TryingStream.TryingStream()).tryIt(Optional::get);
    }

    default TryedStream<T, NoSuchElementException> orElseThrow() {
        return get();
    }

    default <E extends Throwable> TryedStream<T, E> orElseThrow(Supplier<? extends E> exceptionSupplier) {
        return as(TryingStream.TryingStream()).tryIt(optional -> optional.orElseThrow(exceptionSupplier));
    }
}
