package de.yoyosource.streamable.impl;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

interface OptionalBaseStream<T> extends Streamable<Optional<T>> {

    private static <T> Class<OptionalPresentStream<T>> presentType() {
        return (Class<OptionalPresentStream<T>>) (Class) OptionalPresentStream.class;
    }

    default OptionalPresentStream<T> isPresent() {
        return gather(new StreamableGatherer<Optional<T>, Optional<T>>() {
            @Override
            public boolean apply(Optional<T> input, Consumer<Optional<T>> next) {
                if (input.isPresent()) next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<Optional<T>> next) {
            }
        }).as(presentType());
    }

    default OptionalStream<T> filter(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer<Optional<T>, Optional<T>>() {
            @Override
            public boolean apply(Optional<T> input, Consumer<Optional<T>> next) {
                next.accept(input.filter(predicate));
                return false;
            }

            @Override
            public void finish(Consumer<Optional<T>> next) {

            }
        }).as(OptionalStream.type());
    }

    default <U> OptionalStream<U> map(Function<? super T, ? extends U> mapper) {
        return gather(new StreamableGatherer<Optional<T>, Optional<U>>() {
            @Override
            public boolean apply(Optional<T> input, Consumer<Optional<U>> next) {
                next.accept(input.map(mapper));
                return false;
            }

            @Override
            public void finish(Consumer<Optional<U>> next) {
            }
        }).as(OptionalStream.type());
    }

    default <U> OptionalStream<U> flatMap(Function<? super T, ? extends Optional<? extends U>> mapper) {
        return gather(new StreamableGatherer<Optional<T>, Optional<U>>() {
            @Override
            public boolean apply(Optional<T> input, Consumer<Optional<U>> next) {
                next.accept(input.flatMap(mapper));
                return false;
            }

            @Override
            public void finish(Consumer<Optional<U>> next) {
            }
        }).as(OptionalStream.type());
    }

    default OptionalStream<T> or(Supplier<? extends Optional<? extends T>> supplier) {
        return gather(new StreamableGatherer<Optional<T>, Optional<T>>() {
            @Override
            public boolean apply(Optional<T> input, Consumer<Optional<T>> next) {
                next.accept(input.or(supplier));
                return false;
            }

            @Override
            public void finish(Consumer<Optional<T>> next) {
            }
        }).as(OptionalStream.type());
    }

    default Streamable<T> orElse(T other) {
        return gather(new StreamableGatherer<Optional<T>, T>() {
            @Override
            public boolean apply(Optional<T> input, Consumer<T> next) {
                next.accept(input.orElse(other));
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {
            }
        }).as(Streamable.type());
    }

    default Streamable<T> orElseGet(Supplier<? extends T> supplier) {
        return gather(new StreamableGatherer<Optional<T>, T>() {
            @Override
            public boolean apply(Optional<T> input, Consumer<T> next) {
                next.accept(input.orElseGet(supplier));
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        }).as(Streamable.type());
    }
}
