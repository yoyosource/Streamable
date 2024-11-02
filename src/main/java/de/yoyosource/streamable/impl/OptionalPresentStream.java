package de.yoyosource.streamable.impl;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface OptionalPresentStream<T> extends OptionalBaseStream<T> {

    default Streamable<T> get() {
        return gather(new StreamableGatherer<Optional<T>, T>() {
            @Override
            public boolean apply(Optional<T> input, Consumer<T> next) {
                next.accept(input.get());
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        }).as(Streamable.type());
    }

    default Streamable<T> orElseThrow() {
        return get();
    }

    default <E extends Throwable> Streamable<T> orElseThrow(Supplier<? extends E> exceptionSupplier) {
        return get();
    }
}
