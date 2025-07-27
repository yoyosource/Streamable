package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface OptionalBaseStream<N extends OptionalBaseStream<N, T>, T> extends Streamable<N, Optional<T>> {

    /**
     * Returns a {@code OptionalPresentStream<T>} containing no {@code Optional.empty()}.
     *
     * @return the {@code OptionalPresentStream<T>}
     * @see OptionalPresentStream#get()
     * @see OptionalPresentStream#orElseThrow()
     * @see OptionalPresentStream#orElseThrow(Supplier)
     */
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

    /**
     * Filters the {@code OptionalStream<T>} or {@code OptionalPresentStream<T>} and
     * returns a stream containing the elements that match the {@link Predicate}
     * Every element being either {@code Optional.empty()} or not matching the given
     * {@link Predicate} will result in a {@code Optional.empty()}.
     *
     * @param predicate the filter to apply
     * @return the OptionalStream
     */
    default OptionalStream<T> filter(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer.Simple<Optional<T>, Optional<T>>() {
            @Override
            public boolean integrate(long index, Optional<T> input, Consumer<? super Optional<T>> next) {
                next.accept(input.filter(predicate));
                return false;
            }

            @Override
            public void finish(Consumer<? super Optional<T>> next) {
            }
        }).as(OptionalStream.OptionalStream());
    }

    /**
     * Applies the {@link Optional#map(Function)} function to every element
     * present in this Stream.
     *
     * @param mapper the function to apply
     * @return the OptionalStream containing the new elements
     * @param <U> the generic type
     * @see Optional#map(Function)
     */
    default <U> OptionalStream<U> map(Function<? super T, ? extends U> mapper) {
        return gather(new StreamableGatherer.Simple<Optional<T>, Optional<U>>() {
            @Override
            public boolean integrate(long index, Optional<T> input, Consumer<? super Optional<U>> next) {
                next.accept(input.map(mapper));
                return false;
            }

            @Override
            public void finish(Consumer<? super Optional<U>> next) {
            }
        }).as(OptionalStream.OptionalStream());
    }

    /**
     * Applies the {@link Optional#flatMap(Function)} function to every element
     * present in this Stream.
     *
     * @param mapper the function to apply
     * @return the OptionalStream containing the new elements
     * @param <U> the generic type
     * @see Optional#flatMap(Function)
     */
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

    /**
     * Replaces every {@code Optional.empty()} to the value supplied by calling
     * the {@code Supplier}.
     *
     * @param supplier the supplier to be called for {@code Optional.empty()} values.
     * @return the {@code OptionalStream} with the elements
     */
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

    /**
     * Unwraps every {@code Optional} to either the value it holds or the value
     * supplied by the caller.
     *
     * @param other the alternate value to use for {@code Optional.empty()}.
     * @return the {@code JavaStream} with the elements
     */
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

    /**
     * Unwraps every {@code Optional} to either the value it holds or the value
     * supplied by calling the {@code Supplier}.
     *
     * @param supplier the supplier to be called for {@code Optional.empty()} values.
     * @return the {@code JavaStream} with the elements
     */
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
