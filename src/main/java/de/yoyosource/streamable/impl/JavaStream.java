package de.yoyosource.streamable.impl;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.StreamableGatherer;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

public interface JavaStream<T> extends Streamable<T> {

    static <T> Class<JavaStream<T>> type() {
        return (Class<JavaStream<T>>) (Class) JavaStream.class;
    }

    default JavaStream<T> filter(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer<T, T>() {
            @Override
            public boolean apply(T input, Consumer<T> next) {
                if (predicate.test(input)) next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {
            }
        });
    }

    default <R> JavaStream<R> map(Function<? super T, ? extends R> mapper) {
        return gather(new StreamableGatherer<>() {
            @Override
            public boolean apply(T input, Consumer<R> next) {
                next.accept(mapper.apply(input));
                return false;
            }

            @Override
            public void finish(Consumer<R> next) {

            }
        });
    }

    default <R> JavaStream<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return flatGather(new StreamableGatherer<>() {
            @Override
            public boolean apply(T input, Consumer<Iterable<R>> next) {
                next.accept((Iterable<R>) mapper.apply(input));
                return false;
            }

            @Override
            public void finish(Consumer<Iterable<R>> next) {

            }
        });
    }

    default <R> JavaStream<R> mapMulti(BiConsumer<? super T, ? super Consumer<R>> mapper) {
        return gather(new StreamableGatherer<>() {
            @Override
            public boolean apply(T input, Consumer<R> next) {
                mapper.accept(input, next);
                return false;
            }

            @Override
            public void finish(Consumer<R> next) {

            }
        });
    }

    default JavaStream<T> distinct() {
        return gather(new StreamableGatherer<>() {
            private Set<T> elements = new HashSet<>();

            @Override
            public boolean apply(T input, Consumer<T> next) {
                if (elements.add(input)) next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        });
    }

    default JavaStream<T> sorted() { // TODO: Maybe put this into its own Stream?
        return sorted(null);
    }

    default JavaStream<T> sorted(Comparator<? super T> comparator) {
        return flatGather(new StreamableGatherer<>() {
            private List<T> elements = new ArrayList<>();

            @Override
            public boolean apply(T input, Consumer<Iterable<T>> next) {
                elements.add(input);
                return false;
            }

            @Override
            public void finish(Consumer<Iterable<T>> next) {
                elements.sort(comparator);
                next.accept(elements);
            }
        });
    }

    default JavaStream<T> peek(Consumer<? super T> action) {
        return gather(new StreamableGatherer<>() {
            @Override
            public boolean apply(T input, Consumer<T> next) {
                action.accept(input);
                next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        });
    }

    default JavaStream<T> limit(long maxSize) {
        return gather(new StreamableGatherer<>() {
            private long elementsLeft = maxSize;

            @Override
            public boolean apply(T input, Consumer<T> next) {
                next.accept(input);
                elementsLeft--;
                return elementsLeft == 0;
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        });
    }

    default JavaStream<T> skip(long skip) {
        return gather(new StreamableGatherer<>() {
            private long elementsLeft = skip;

            @Override
            public boolean apply(T input, Consumer<T> next) {
                elementsLeft--;
                if (elementsLeft < 0) next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        });
    }

    default JavaStream<T> takeWhile(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer<>() {
            @Override
            public boolean apply(T input, Consumer<T> next) {
                if (predicate.test(input)) {
                    next.accept(input);
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        });
    }

    default JavaStream<T> dropWhile(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer<>() {
            private boolean take = false;

            @Override
            public boolean apply(T input, Consumer<T> next) {
                if (predicate.test(input)) take = true;
                if (take) next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        });
    }

    default Object[] toArray() {
        return toArray(Object[]::new);
    }

    default <A> A[] toArray(IntFunction<A[]> generator) {
        return collect(new StreamableCollector<>() {
            private List<T> elements = new ArrayList<>();

            @Override
            public boolean apply(T input) {
                elements.add(input);
                return false;
            }

            @Override
            public A[] finish() {
                return elements.toArray(generator);
            }
        });
    }

    default T reduce(T identity, BinaryOperator<T> accumulator) {
        return collect(new StreamableCollector<>() {
            private T current = identity;

            @Override
            public boolean apply(T input) {
                current = accumulator.apply(current, input);
                return false;
            }

            @Override
            public T finish() {
                return current;
            }
        });
    }

    default Optional<T> reduce(BinaryOperator<T> accumulator) {
        return collect(new StreamableCollector<>() {
            private T current = null;

            @Override
            public boolean apply(T input) {
                if (current == null) {
                    current = input;
                } else {
                    accumulator.apply(current, input);
                }
                return false;
            }

            @Override
            public Optional<T> finish() {
                return Optional.ofNullable(current);
            }
        });
    }

    default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator) {
        return collect(new StreamableCollector<>() {
            private U current = identity;

            @Override
            public boolean apply(T input) {
                current = accumulator.apply(current, input);
                return false;
            }

            @Override
            public U finish() {
                return current;
            }
        });
    }

    default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator) {
        return collect(new StreamableCollector<>() {
            private R current = supplier.get();

            @Override
            public boolean apply(T input) {
                accumulator.accept(current, input);
                return false;
            }

            @Override
            public R finish() {
                return current;
            }
        });
    }

    default <R, A> R collect(Collector<? super T, A, R> collector) {
        return collect(new StreamableCollector<>() {
            private A current = collector.supplier().get();

            @Override
            public boolean apply(T input) {
                collector.accumulator().accept(current, input);
                return false;
            }

            @Override
            public R finish() {
                return collector.finisher().apply(current);
            }
        });
    }

    default List<T> toList() {
        return collect(new StreamableCollector<>() {
            private List<T> elements = new ArrayList<>();

            @Override
            public boolean apply(T input) {
                elements.add(input);
                return false;
            }

            @Override
            public List<T> finish() {
                return elements;
            }
        });
    }

    default Optional<T> min(Comparator<? super T> comparator) {
        return collect(new StreamableCollector<>() {
            private T current = null;

            @Override
            public boolean apply(T input) {
                if (current == null) {
                    current = input;
                    return false;
                }

                if (comparator.compare(current, input) > 0) {
                    current = input;
                }
                return false;
            }

            @Override
            public Optional<T> finish() {
                return Optional.ofNullable(current);
            }
        });
    }

    default Optional<T> max(Comparator<? super T> comparator) {
        return collect(new StreamableCollector<>() {
            private T current = null;

            @Override
            public boolean apply(T input) {
                if (current == null) {
                    current = input;
                    return false;
                }

                if (comparator.compare(current, input) < 0) {
                    current = input;
                }
                return false;
            }

            @Override
            public Optional<T> finish() {
                return Optional.ofNullable(current);
            }
        });
    }

    default long count() {
        return collect(new StreamableCollector<>() {
            private long count = 0;

            @Override
            public boolean apply(T input) {
                count++;
                return false;
            }

            @Override
            public Long finish() {
                return count;
            }
        });
    }

    default boolean anyMatch(Predicate<? super T> predicate) {
        return collect(new StreamableCollector<>() {
            private boolean anyMatch = false;

            @Override
            public boolean apply(T input) {
                if (predicate.test(input)) {
                    anyMatch = true;
                    return true;
                }
                return false;
            }

            @Override
            public Boolean finish() {
                return anyMatch;
            }
        });
    }

    default boolean allMatch(Predicate<? super T> predicate) {
        return !anyMatch(predicate.negate());
    }

    default boolean noneMatch(Predicate<? super T> predicate) {
        return !anyMatch(predicate);
    }

    default Optional<T> findFirst() {
        return collect(new StreamableCollector<>() {
            private T current = null;

            @Override
            public boolean apply(T input) {
                current = input;
                return true;
            }

            @Override
            public Optional<T> finish() {
                return Optional.ofNullable(current);
            }
        });
    }

    default Optional<T> findAny() {
        return findFirst();
    }

    default JavaStream<T> onClose(Runnable action) {
        return gather(new StreamableGatherer<>() {
            @Override
            public boolean apply(T input, Consumer<T> next) {
                next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {

            }

            @Override
            public void onClose() {
                action.run();
            }
        });
    }
}
