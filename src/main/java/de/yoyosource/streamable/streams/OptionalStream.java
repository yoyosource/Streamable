package de.yoyosource.streamable.streams;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public interface OptionalStream<T> extends OptionalBaseStream<OptionalStream<T>, T> {

    /**
     * Returns a {@code Class} instance with the generic type of {@code OptionalStream} for {@link #as(Class)} method.
     *
     * @param <T> the type of elements inside the {@code OptionalStream}
     * @return the type for {@link #as(Class)}
     */
    static <T> Class<OptionalStream<T>> OptionalStream() {
        return (Class<OptionalStream<T>>) (Class) OptionalStream.class;
    }

    /**
     * Returns a {@code Class} instance with the generic type of {@code OptionalStream} for {@link #as(Class)} method.
     *
     * @param <T> the type of elements inside the {@code OptionalStream}
     * @param clazz the type {@code T} should be
     * @return the type for {@link #as(Class)}
     */
    static <T> Class<OptionalStream<T>> OptionalStream(Class<T> clazz) {
        return (Class<OptionalStream<T>>) (Class) OptionalStream.class;
    }

    /**
     * Returns a {@code TryedStream<T, NoSuchElementException>} with
     * {@code Try#Success(T)} for any {@link Optional#isPresent()} and with
     * {@code Try#Failure(NoSuchElementException)} for any {@link Optional#isEmpty()}.
     *
     * @apiNote
     * The preferred alternative to this method is {@link #orElseThrow()}.
     *
     * @return the {@code TryedStream} containing either {@code T} or {@code NoSuchElementException}
     */
    default TryedStream<T, NoSuchElementException> get() {
        return as(TryingStream.TryingStream()).tryIt(Optional::get);
    }

    /**
     * Returns a {@code TryedStream<T, NoSuchElementException>} with
     * {@code Try#Success(T)} for any {@link Optional#isPresent()} and with
     * {@code Try#Failure(NoSuchElementException)} for any {@link Optional#isEmpty()}.
     *
     * @apiNote
     * The preferred alternative to this method is {@link #orElseThrow()}.
     *
     * @return the {@code TryedStream} containing either {@code T} or {@code NoSuchElementException}
     */
    default TryedStream<T, NoSuchElementException> orElseThrow() {
        return get();
    }

    /**
     * Returns a {@code TryedStream<T, E>} with
     * {@code Try#Success(T)} for any {@link Optional#isPresent()} and with
     * {@code Try#Failure(E)} for any {@link Optional#isEmpty()}.
     *
     * @apiNote
     * The preferred alternative to this method is {@link #orElseThrow()}.
     *
     * @param <E> The Exception type.
     * @return the {@code TryedStream} containing either {@code T} or {@code E}
     */
    default <E extends Throwable> TryedStream<T, E> orElseThrow(Supplier<? extends E> exceptionSupplier) {
        return as(TryingStream.TryingStream()).tryIt(optional -> optional.orElseThrow(exceptionSupplier));
    }
}
