package de.yoyosource.streamable3;

import de.yoyosource.streamable3.streams.JavaStream;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static de.yoyosource.streamable3.streams.JavaStream.JavaStream;

public interface Streamable<S extends Streamable<S, T>, T> extends Iterable<T> {

    static <T> JavaStream<T> empty() {
        return StreamableManager.from(Collections.<T>emptyIterator())
                .as(JavaStream());
    }

    static <T> JavaStream<T> of(T element) {
        return StreamableManager.from(Collections.singletonList(element).iterator())
                .as(JavaStream());
    }

    static <T> JavaStream<T> ofNullable(T element) {
        return element == null ? empty() : of(element);
    }

    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a stream from an array is safe
    static <T> JavaStream<T> of(T... elements) {
        return StreamableManager.from(Arrays.stream(elements).iterator())
                .as(JavaStream());
    }

    static <T> JavaStream<T> iterate(final T seed, final UnaryOperator<T> f) {
        return iterate(seed, t -> true, f);
    }

    static <T> JavaStream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        return StreamableManager.from(new Iterator<T>() {
            private T current = seed;

            @Override
            public boolean hasNext() {
                return hasNext.test(current);
            }

            @Override
            public T next() {
                T previous = current;
                current = next.apply(current);
                return previous;
            }
        }).as(JavaStream());
    }

    static <T> JavaStream<T> generate(Supplier<? extends T> s) {
        return iterate(s.get(), t -> true, t -> s.get());
    }

    static <T> JavaStream<T> from(Stream<T> stream) {
        return StreamableManager.from(stream.iterator())
                .as(JavaStream());
    }

    static <T> JavaStream<T> from(Iterable<T> iterable) {
        return StreamableManager.from(iterable.iterator())
                .as(JavaStream());
    }

    static <T> JavaStream<T> from(Iterator<T> iterator) {
        return StreamableManager.from(iterator)
                .as(JavaStream());
    }

    <N extends Streamable<N, T>> N as(Class<N> clazz);

    S sequential();
    S parallel(int maxParallelism);

    <R, C, N extends Streamable<N, R>> N gather(StreamableGatherer<? super T, C, R> gatherer);
    <R, C, N extends Streamable<N, R>> N flatGather(StreamableGatherer<? super T, C, Iterable<R>> gatherer);
    <R, C> R collect(StreamableCollector<? super T, C, R> collector);

    @Override
    default void forEach(Consumer<? super T> action) {
        collect(new StreamableCollector.Simple<T, T>() {
            @Override
            public boolean accumulate(long index, T element) {
                action.accept(element);
                return false;
            }

            @Override
            public T finish() {
                return null;
            }
        });
    }
}
