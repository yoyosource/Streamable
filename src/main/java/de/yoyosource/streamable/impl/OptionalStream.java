package de.yoyosource.streamable.impl;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public interface OptionalStream<T> extends OptionalBaseStream<T> {

    static <T> Class<OptionalStream<T>> type() {
        return (Class<OptionalStream<T>>) (Class) OptionalStream.class;
    }

    default TryedStream<T, NoSuchElementException> get() {
        return as(TryingStream.type()).tryIt(Optional::get);
    }

    default TryedStream<T, NoSuchElementException> orElseThrow() {
        return get();
    }

    default <E extends Throwable> TryedStream<T, E> orElseThrow(Supplier<? extends E> exceptionSupplier) {
        return as(TryingStream.type()).tryIt(optional -> optional.orElseThrow(exceptionSupplier));
    }
}
