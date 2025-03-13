package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.StreamableGatherer;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface OptionalPresentStream<T> extends OptionalBaseStream<OptionalPresentStream<T>, T> {

    default <U> OptionalPresentStream<U> map(Function<? super T, ? extends U> mapper) {
        return OptionalBaseStream.super._map(mapper);
    }

    default JavaStream<T> get() {
        return gather(new StreamableGatherer.Simple<Optional<T>, T>() {
            @Override
            public boolean integrate(long index, Optional<T> input, Consumer<? super T> next) {
                next.accept(input.get());
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {

            }
        }).as(JavaStream.JavaStream());
    }

    default JavaStream<T> orElseThrow() {
        return get();
    }

    default <E extends Throwable> JavaStream<T> orElseThrow(Supplier<? extends E> exceptionSupplier) {
        return get();
    }
}
