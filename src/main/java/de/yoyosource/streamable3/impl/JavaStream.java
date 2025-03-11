package de.yoyosource.streamable3.impl;

import de.yoyosource.streamable3.Streamable;
import de.yoyosource.streamable3.StreamableCollector;
import de.yoyosource.streamable3.StreamableGatherer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

public interface JavaStream<T> extends Streamable<JavaStream<T>, T> {

    static <T> Class<JavaStream<T>> JavaStream() {
        return (Class<JavaStream<T>>) (Class) JavaStream.class;
    }

    default JavaStream<T> filter(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                if (predicate.test(element)) next.accept(element);
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    default <R> JavaStream<R> map(Function<? super T, ? extends R> mapper) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T element, Consumer<? super R> next) {
                next.accept(mapper.apply(element));
                return false;
            }

            @Override
            public void finish(Consumer<? super R> next) {
            }
        });
    }

    default <R> JavaStream<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return flatGather(new StreamableGatherer.Simple<T, Iterable<R>>() {
            @Override
            public boolean integrate(long index, T element, Consumer<? super Iterable<R>> next) {
                next.accept((Iterable<R>) mapper.apply(element));
                return false;
            }

            @Override
            public void finish(Consumer<? super Iterable<R>> next) {
            }
        });
    }
    
    default <R> JavaStream<R> mapMulti(BiConsumer<? super T, ? super Consumer<? super R>> mapper) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T input, Consumer<? super R> next) {
                mapper.accept(input, next);
                return false;
            }

            @Override
            public void finish(Consumer<? super R> next) {
            }
        });
    }

    default JavaStream<T> distinct() {
        return gather(new StreamableGatherer.Simple<>() {
            private Set<T> elements = new HashSet<>();

            @Override
            public boolean integrate(long index, T input, Consumer<? super T> next) {
                if (elements.add(input)) next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    default JavaStream<T> sorted(Comparator<? super T> comparator) {
        return flatGather(new StreamableGatherer.Simple<>() {
            private List<T> elements = new ArrayList<>();

            @Override
            public boolean integrate(long index, T input, Consumer<? super Iterable<T>> next) {
                elements.add(input);
                return false;
            }

            @Override
            public void finish(Consumer<? super Iterable<T>> next) {
                elements.sort(comparator);
                next.accept(elements);
            }
        });
    }

    default JavaStream<T> peek(Consumer<? super T> action) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T input, Consumer<? super T> next) {
                action.accept(input);
                next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    default JavaStream<T> limit(long maxSize) {
        return gather(new StreamableGatherer.Simple<>() {
            private long elementsLeft = maxSize;

            @Override
            public boolean integrate(long index, T input, Consumer<? super T> next) {
                next.accept(input);
                elementsLeft--;
                return elementsLeft == 0;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    default JavaStream<T> skip(long skip) {
        return gather(new StreamableGatherer.Simple<>() {
            private long elementsLeft = skip;

            @Override
            public boolean integrate(long index, T input, Consumer<? super T> next) {
                elementsLeft--;
                if (elementsLeft < 0) next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    default JavaStream<T> takeWhile(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T input, Consumer<? super T> next) {
                if (predicate.test(input)) {
                    next.accept(input);
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    default JavaStream<T> dropWhile(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            private boolean take = false;

            @Override
            public boolean integrate(long index, T input, Consumer<? super T> next) {
                if (predicate.test(input)) take = true;
                if (take) next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    default Object[] toArray() {
        return toArray(Object[]::new);
    }

    default <A> A[] toArray(IntFunction<A[]> generator) {
        return collect(new StreamableCollector.Simple<>() {
            private List<T> elements = new ArrayList<>();

            @Override
            public boolean accumulate(long index, T input) {
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
        return collect(new StreamableCollector.Simple<>() {
            private T current = identity;

            @Override
            public boolean accumulate(long index, T input) {
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
        return collect(new StreamableCollector.Simple<>() {
            private T current = null;

            @Override
            public boolean accumulate(long index, T input) {
                if (current == null) {
                    current = input;
                } else {
                    current = accumulator.apply(current, input);
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
        return collect(new StreamableCollector.Simple<>() {
            private U current = identity;

            @Override
            public boolean accumulate(long index, T input) {
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
        return collect(new StreamableCollector.Simple<>() {
            private R current = supplier.get();

            @Override
            public boolean accumulate(long index, T input) {
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
        return collect(new StreamableCollector.Simple<>() {
            private A current = collector.supplier().get();

            @Override
            public boolean accumulate(long index, T input) {
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
        return collect(new StreamableCollector.Simple<>() {
            private List<T> elements = new ArrayList<>();

            @Override
            public boolean accumulate(long index, T input) {
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
        return collect(new StreamableCollector.Simple<>() {
            private T current = null;

            @Override
            public boolean accumulate(long index, T input) {
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
        return collect(new StreamableCollector.Simple<>() {
            private T current = null;

            @Override
            public boolean accumulate(long index, T input) {
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
        return collect(new StreamableCollector.Simple<>() {
            private long count = 0;

            @Override
            public boolean accumulate(long index, T input) {
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
        return collect(new StreamableCollector.Simple<>() {
            private boolean anyMatch = false;

            @Override
            public boolean accumulate(long index, T input) {
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
        return collect(new StreamableCollector.Simple<>() {
            private T current = null;

            @Override
            public boolean accumulate(long index, T input) {
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
        // TODO: Optimize this for multithreading element before!
        return findFirst();
    }

    default Optional<T> findLast() {
        return collect(new StreamableCollector.Simple<>() {
            private T current = null;

            @Override
            public boolean accumulate(long index, T input) {
                current = input;
                return false;
            }

            @Override
            public Optional<T> finish() {
                return Optional.ofNullable(current);
            }
        });
    }
}
