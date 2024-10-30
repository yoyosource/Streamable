package de.yoyosource.streamable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public interface Streamable<T> extends Iterable<T> {
    static <T> Streamable<T> empty() {
        return StreamableManager.from(Collections.emptyIterator());
    }

    static <T> Streamable<T> of(T element) {
        return StreamableManager.from(Collections.singletonList(element).iterator());
    }

    static <T> Streamable<T> ofNullable(T element) {
        return element == null ? empty() : of(element);
    }

    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a stream from an array is safe
    static <T> Streamable<T> of(T... elements) {
        return StreamableManager.from(Arrays.stream(elements).iterator());
    }

    static <T> Streamable<T> iterate(final T seed, final UnaryOperator<T> f) {
        return iterate(seed, t -> true, f);
    }

    static <T> Streamable<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        return StreamableManager.from(new Iterator<>() {
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
        });
    }

    static <T> Streamable<T> generate(Supplier<? extends T> s) {
        return iterate(s.get(), t -> true, t -> s.get());
    }

    static <T> Streamable<T> from(Stream<T> stream) {
        return StreamableManager.from(stream.iterator());
    }

    static <T> Streamable<T> from(Iterable<T> iterable) {
        return StreamableManager.from(iterable.iterator());
    }

    static <T> Streamable<T> from(Iterator<T> iterator) {
        return StreamableManager.from(iterator);
    }

    <R, S extends Streamable<R>> S gather(StreamableGatherer<? super T, R> gatherer);
    <R, S extends Streamable<R>> S flatGather(StreamableGatherer<? super T, Iterable<R>> gatherer);
    <R> R collect(StreamableCollector<? super T, R> collector);

    <S extends Streamable<T>> S as(Class<S> clazz);

    @Override
    default void forEach(Consumer<? super T> action) {
        collect(new StreamableCollector<>() {
            @Override
            public boolean apply(T input) {
                action.accept(input);
                return false;
            }

            @Override
            public Object finish() {
                return null;
            }
        });
    }
}
