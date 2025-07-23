package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface OptionalBaseStream<N extends OptionalBaseStream<N, T>, T> extends Streamable<N, Optional<T>> {

    @SuppressWarnings({"unchecked"})
    default OptionalPresentStream<T> isPresent() {
        return gather(new StreamableGatherer.Simple<Optional<T>, Optional<T>>() {
            @Override
            public boolean integrate(long index, Optional<T> element, Consumer<? super Optional<T>> next) {
                if (element.isPresent()) next.accept(element);
                return false;
            }

            @Override
            public void finish(Consumer<? super Optional<T>> next) {

            }
        }).as((Class<OptionalPresentStream<T>>) (Class) OptionalPresentStream.class);
    }

    default N filter(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, Optional<T> input, Consumer<? super Optional<T>> next) {
                next.accept(input.filter(predicate));
                return false;
            }

            @Override
            public void finish(Consumer<? super Optional<T>> next) {
            }
        });
    }

    default <S extends OptionalBaseStream<S, U>, U> S _map(Function<? super T, ? extends U> mapper) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, Optional<T> input, Consumer<? super Optional<U>> next) {
                next.accept(input.map(mapper));
                return false;
            }

            @Override
            public void finish(Consumer<? super Optional<U>> next) {
            }
        });
    }

    @SuppressWarnings("unchecked")
    default <U> OptionalStream<U> flatMap(Function<? super T, ? extends Optional<? extends U>> mapper) {
        return gather(new StreamableGatherer.Simple<Optional<T>, Optional<U>>() {
            @Override
            public boolean integrate(long index, Optional<T> input, Consumer<? super Optional<U>> next) {
                next.accept(input.flatMap(mapper));
                return false;
            }

            @Override
            public void finish(Consumer<? super Optional<U>> next) {
            }
        }).as(OptionalStream.OptionalStream());
    }

    @SuppressWarnings("unchecked")
    default OptionalStream<T> or(Supplier<? extends Optional<? extends T>> supplier) {
        return gather(new StreamableGatherer.Simple<Optional<T>, Optional<T>>() {
            @Override
            public boolean integrate(long index, Optional<T> input, Consumer<? super Optional<T>> next) {
                next.accept(input.or(supplier));
                return false;
            }

            @Override
            public void finish(Consumer<? super Optional<T>> next) {
            }
        }).as(OptionalStream.OptionalStream());
    }

    @SuppressWarnings("unchecked")
    default JavaStream<T> orElse(T other) {
        return gather(new StreamableGatherer.Simple<Optional<T>, T>() {
            @Override
            public boolean integrate(long index, Optional<T> input, Consumer<? super T> next) {
                next.accept(input.orElse(other));
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        }).as(JavaStream.JavaStream());
    }

    @SuppressWarnings("unchecked")
    default JavaStream<T> orElseGet(Supplier<? extends T> supplier) {
        return gather(new StreamableGatherer.Simple<Optional<T>, T>() {
            @Override
            public boolean integrate(long index, Optional<T> input, Consumer<? super T> next) {
                next.accept(input.orElseGet(supplier));
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {

            }
        }).as(JavaStream.JavaStream());
    }
}
