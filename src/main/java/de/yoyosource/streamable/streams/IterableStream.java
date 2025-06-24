package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public interface IterableStream<T> extends Streamable<IterableStream<T>, Iterable<T>> {

    static <T> Class<IterableStream<T>> IterableStream() {
        return (Class<IterableStream<T>>) (Class) IterableStream.class;
    }

    static <T> Class<IterableStream<T>> IterableStream(Class<T> clazz) {
        return (Class<IterableStream<T>>) (Class) IterableStream.class;
    }

    static <T> JavaStream<T> toStreamable(Iterable<T> iterable) {
        if (iterable instanceof JavaStream<T> javaStream) {
            return javaStream;
        } else if (iterable instanceof Streamable<?, T> streamable) {
            return streamable.as(JavaStream.JavaStream());
        } else {
            return Streamable.from(iterable);
        }
    }

    default IterableStream<T> sequentialEach() {
        return gatherEach(Streamable::sequential);
    }

    default IterableStream<T> parallelEach(int maxParallelism) {
        return gatherEach(ts -> ts.parallel(maxParallelism));
    }

    default <R> IterableStream<R> gatherEach(Function<JavaStream<T>, Streamable<?, R>> mapper) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, Iterable<T> element, Consumer<? super Iterable<R>> next) {
                next.accept(mapper.apply(toStreamable(element)));
                return false;
            }

            @Override
            public void finish(Consumer<? super Iterable<R>> next) {
            }
        });
    }

    default <R> JavaStream<R> collectEach(Function<JavaStream<T>, R> mapper) {
        return gather(new StreamableGatherer.Simple<Iterable<T>, R>() {
            @Override
            public boolean integrate(long index, Iterable<T> element, Consumer<? super R> next) {
                next.accept(mapper.apply(toStreamable(element)));
                return false;
            }

            @Override
            public void finish(Consumer<? super R> next) {
            }
        }).as(JavaStream.JavaStream());
    }

    default JavaStream<T> flatten() {
        return flatGather(new StreamableGatherer.Simple<Iterable<T>, Iterable<T>>() {
            @Override
            public boolean integrate(long index, Iterable<T> element, Consumer<? super Iterable<T>> next) {
                next.accept(element);
                return false;
            }

            @Override
            public void finish(Consumer<? super Iterable<T>> next) {
            }
        }).as(JavaStream.JavaStream());
    }

    default IterableStream<T> filter(Predicate<? super T> predicate) {
        return gatherEach(ts -> ts.filter(predicate));
    }

    default <R> IterableStream<R> map(Function<? super T, ? extends R> mapper) {
        return gatherEach(ts -> ts.map(mapper));
    }

    default <R> IterableStream<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return gatherEach(ts -> ts.flatMap(mapper));
    }

    default <R> IterableStream<R> mapMulti(BiConsumer<? super T, ? super Consumer<? super R>> mapper) {
        return gatherEach(ts -> ts.mapMulti(mapper));
    }

    default IterableStream<T> distinct() {
        return gatherEach(JavaStream::distinct);
    }

    default IterableStream<T> sorted(Comparator<? super T> comparator) {
        return gatherEach(ts -> ts.sorted(comparator));
    }

    default IterableStream<T> peek(Consumer<? super T> action) {
        return gatherEach(ts -> ts.peek(action));
    }

    default IterableStream<T> limit(long maxSize) {
        return gatherEach(ts -> ts.limit(maxSize));
    }

    default IterableStream<T> skip(long skip) {
        return gatherEach(ts -> ts.skip(skip));
    }

    default IterableStream<T> takeWhile(Predicate<? super T> predicate) {
        return gatherEach(ts -> ts.takeWhile(predicate));
    }

    default IterableStream<T> dropWhile(Predicate<? super T> predicate) {
        return gatherEach(ts -> ts.dropWhile(predicate));
    }

    default JavaStream<Object[]> toArray() {
        return toArray(Object[]::new);
    }

    default <A> JavaStream<A[]> toArray(IntFunction<A[]> generator) {
        return collectEach(ts -> ts.toArray(generator));
    }

    default JavaStream<T> reduce(T identity, BinaryOperator<T> accumulator) {
        return collectEach(ts -> ts.reduce(identity, accumulator));
    }

    default OptionalStream<T> reduce(BinaryOperator<T> accumulator) {
        return collectEach(ts -> ts.reduce(accumulator))
                .as(OptionalStream.OptionalStream());
    }

    default <R, A> JavaStream<R> collect(Collector<? super T, A, R> collector) {
        return collectEach(ts -> ts.collect(collector));
    }

    default JavaStream<List<T>> toList() {
        return collect(Collectors.toList());
    }

    default OptionalStream<T> min(Comparator<? super T> comparator) {
        return collectEach(ts -> ts.min(comparator))
                .as(OptionalStream.OptionalStream());
    }

    default OptionalStream<T> max(Comparator<? super T> comparator) {
        return collectEach(ts -> ts.max(comparator))
                .as(OptionalStream.OptionalStream());
    }

    default NumberStream<Long> count() {
        return collectEach(JavaStream::count)
                .as(NumberStream.NumberStream());
    }

    default JavaStream<Boolean> anyMatch(Predicate<? super T> predicate) {
        return collectEach(ts -> ts.anyMatch(predicate));
    }

    default JavaStream<Boolean> allMatch(Predicate<? super T> predicate) {
        return collectEach(ts -> ts.allMatch(predicate));
    }

    default JavaStream<Boolean> noneMatch(Predicate<? super T> predicate) {
        return collectEach(ts -> ts.noneMatch(predicate));
    }

    default OptionalStream<T> findFirst() {
        return collectEach(JavaStream::findFirst)
                .as(OptionalStream.OptionalStream());
    }

    default OptionalStream<T> findAny() {
        return collectEach(JavaStream::findAny)
                .as(OptionalStream.OptionalStream());
    }

    default OptionalStream<T> findLast() {
        return collectEach(JavaStream::findLast)
                .as(OptionalStream.OptionalStream());
    }
}
