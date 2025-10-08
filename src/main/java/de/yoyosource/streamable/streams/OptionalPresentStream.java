package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.StreamableGatherer;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface OptionalPresentStream<T> extends OptionalBaseStream<OptionalPresentStream<T>, T> {

    /**
     * Returns a {@code JavaStream<T>} by applying {@link Optional#get()} on
     * all elements of the Stream.
     *
     * @apiNote
     * The preferred alternative to this method is {@link #orElseThrow()}.
     *
     * @return the {@code JavaStream} containing {@code T}
     */
    default JavaStream<T> get() {
        return gather(new StreamableGatherer.Simple<Optional<T>, T>() {
            @Override
            public boolean integrate(long index, Optional<T> input, Consumer<? super T> next) {
                next.accept(input.get());
                return true;
            }

            @Override
            public void finish(Consumer<? super T> next) {

            }
        }).as(JavaStream.JavaStream());
    }

    /**
     * Returns a {@code JavaStream<T>} by applying {@link Optional#get()} on
     * all elements of the Stream.
     *
     * @apiNote
     * The preferred alternative to this method is {@link #orElseThrow()}.
     *
     * @return the {@code JavaStream} containing {@code T}
     */
    default JavaStream<T> orElseThrow() {
        return get();
    }

    /**
     * Returns a {@code JavaStream<T>} by applying {@link Optional#get()} on
     * all elements of the Stream.
     *
     * @apiNote
     * The preferred alternative to this method is {@link #orElseThrow()}.
     *
     * @param <E> the Exception type.
     * @return the {@code JavaStream} containing {@code T}
     */
    default <E extends Throwable> JavaStream<T> orElseThrow(Supplier<? extends E> exceptionSupplier) {
        return get();
    }

    /**
     * {@inheritDoc}
     *
     * @param other the alternate value to use for {@code Optional.empty()}.
     * @return the {@code JavaStream} with the elements
     */
    @Override
    default JavaStream<T> orElse(T other) {
        return get();
    }

    /**
     * {@inheritDoc}
     *
     * @param supplier the supplier to be called for {@code Optional.empty()} values.
     * @return the {@code JavaStream} with the elements
     */
    @Override
    default JavaStream<T> orElseGet(Supplier<? extends T> supplier) {
        return get();
    }
}
