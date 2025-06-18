package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.data.SingleData;
import de.yoyosource.streamable.internal.InternalStreamable;
import de.yoyosource.streamable.internal.InternalStreamableCollector;
import de.yoyosource.streamable.internal.finish.FindAnyFinish;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public interface JavaStream<T> extends Streamable<JavaStream<T>, T> {

    static <T> Class<JavaStream<T>> JavaStream() {
        return (Class<JavaStream<T>>) (Class) JavaStream.class;
    }

    static <T> Class<JavaStream<T>> JavaStream(Class<T> clazz) {
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
        return flatGather(new StreamableGatherer<T, List<T>, Iterable<T>>() {
            @Override
            public List<T> container() {
                return new ArrayList<>();
            }

            @Override
            public boolean integrate(List<T> container, long index, T element, Consumer<? super Iterable<T>> next) {
                container.add(element);
                return false;
            }

            @Override
            public List<T> combine(List<T> firstContainer, List<T> secondContainer) {
                firstContainer.addAll(secondContainer);
                return firstContainer;
            }

            @Override
            public void finish(List<T> container, Consumer<? super Iterable<T>> next) {
                container.sort(comparator);
                next.accept(container);
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
        if (maxSize < 0) {
            throw new IllegalArgumentException("Size cannot be negative!");
        }
        if (maxSize == 0) {
            return gather(new StreamableGatherer.Simple<>() {
                @Override
                public boolean integrate(long index, T element, Consumer<? super T> next) {
                    return true;
                }

                @Override
                public void finish(Consumer<? super T> next) {
                }
            });
        } else {
            return gather(new StreamableGatherer.Simple<>() {
                @Override
                public boolean integrate(long index, T input, Consumer<? super T> next) {
                    next.accept(input);
                    return index == maxSize - 1;
                }

                @Override
                public void finish(Consumer<? super T> next) {
                }
            });
        }
    }

    default JavaStream<T> skip(long skip) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip cannot be negative!");
        } else if (skip == 0) {
            return gather(new StreamableGatherer.Simple<>() {
                @Override
                public boolean integrate(long index, T element, Consumer<? super T> next) {
                    next.accept(element);
                    return false;
                }

                @Override
                public void finish(Consumer<? super T> next) {
                }
            });
        } else {
            return gather(new StreamableGatherer.Simple<>() {
                @Override
                public boolean integrate(long index, T input, Consumer<? super T> next) {
                    if (index >= skip) next.accept(input);
                    return false;
                }

                @Override
                public void finish(Consumer<? super T> next) {
                }
            });
        }
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

    @SuppressWarnings("unchecked")
    default JavaStream<T> dropWhile(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer.Sequential.Simple<>() {
            private boolean take = false;

            @Override
            public boolean integrate(long index, T input, Consumer<? super T> next) {
                if (!predicate.test(input)) take = true;
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
        return collect(new StreamableCollector<T, SingleData<T>, T>() {
            @Override
            public SingleData<T> container() {
                return new SingleData<>(null);
            }

            @Override
            public boolean accumulate(SingleData<T> container, long index, T element) {
                if (container.first == null) {
                    container.first = element;
                } else {
                    container.first = accumulator.apply(container.first, element);
                }
                return false;
            }

            @Override
            public SingleData<T> combine(SingleData<T> firstContainer, SingleData<T> secondContainer) {
                if (firstContainer.first == null) {
                    firstContainer.first = secondContainer.first;
                } else {
                    firstContainer.first = accumulator.apply(firstContainer.first, secondContainer.first);
                }
                return firstContainer;
            }

            @Override
            public T finish(SingleData<T> container) {
                if (container.first == null) {
                    return identity;
                } else {
                    return accumulator.apply(identity, container.first);
                }
            }
        });
    }

    default Optional<T> reduce(BinaryOperator<T> accumulator) {
        return collect(new StreamableCollector<T, SingleData<T>, Optional<T>>() {
            @Override
            public SingleData<T> container() {
                return new SingleData<>(null);
            }

            @Override
            public boolean accumulate(SingleData<T> container, long index, T element) {
                if (container.first == null) {
                    container.first = element;
                } else {
                    container.first = accumulator.apply(container.first, element);
                }
                return false;
            }

            @Override
            public SingleData<T> combine(SingleData<T> firstContainer, SingleData<T> secondContainer) {
                if (firstContainer.first == null) {
                    firstContainer.first = secondContainer.first;
                } else {
                    firstContainer.first = accumulator.apply(firstContainer.first, secondContainer.first);
                }
                return firstContainer;
            }

            @Override
            public Optional<T> finish(SingleData<T> container) {
                return Optional.ofNullable(container.first);
            }
        });
    }

    default <R, A> R collect(Collector<? super T, A, R> collector) {
        return collect(new StreamableCollector<T, A, R>() {
            @Override
            public A container() {
                return collector.supplier().get();
            }

            @Override
            public boolean accumulate(A container, long index, T element) {
                collector.accumulator().accept(container, element);
                return false;
            }

            @Override
            public A combine(A firstContainer, A secondContainer) {
                return collector.combiner().apply(firstContainer, secondContainer);
            }

            @Override
            public R finish(A container) {
                return collector.finisher().apply(container);
            }
        });
    }

    default List<T> toList() {
        return collect(Collectors.toList());
    }

    default Optional<T> min(Comparator<? super T> comparator) {
        return collect(new StreamableCollector<T, SingleData<T>, Optional<T>>() {
            @Override
            public SingleData<T> container() {
                return new SingleData<>(null);
            }

            @Override
            public boolean accumulate(SingleData<T> container, long index, T element) {
                if (container.first == null || comparator.compare(container.first, element) > 0) {
                    container.first = element;
                }
                return false;
            }

            @Override
            public SingleData<T> combine(SingleData<T> firstContainer, SingleData<T> secondContainer) {
                if (comparator.compare(firstContainer.first, secondContainer.first) > 0) {
                    return secondContainer;
                } else {
                    return firstContainer;
                }
            }

            @Override
            public Optional<T> finish(SingleData<T> container) {
                return Optional.ofNullable(container.first);
            }
        });
    }

    default Optional<T> max(Comparator<? super T> comparator) {
        return collect(new StreamableCollector<T, SingleData<T>, Optional<T>>() {
            @Override
            public SingleData<T> container() {
                return new SingleData<>(null);
            }

            @Override
            public boolean accumulate(SingleData<T> container, long index, T element) {
                if (container.first == null || comparator.compare(container.first, element) < 0) {
                    container.first = element;
                }
                return false;
            }

            @Override
            public SingleData<T> combine(SingleData<T> firstContainer, SingleData<T> secondContainer) {
                if (comparator.compare(firstContainer.first, secondContainer.first) < 0) {
                    return secondContainer;
                } else {
                    return firstContainer;
                }
            }

            @Override
            public Optional<T> finish(SingleData<T> container) {
                return Optional.ofNullable(container.first);
            }
        });
    }

    default long count() {
        return collect(new StreamableCollector.Sequential.Simple<>() {
            private long count = 0;

            @Override
            public boolean accumulate(long index, T element) {
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
        return collect(new StreamableCollector<T, SingleData<Boolean>, Boolean>() {
            @Override
            public SingleData<Boolean> container() {
                return new SingleData<>(false);
            }

            @Override
            public boolean accumulate(SingleData<Boolean> container, long index, T element) {
                if (predicate.test(element)) {
                    container.first = true;
                    return true;
                }
                return false;
            }

            @Override
            public SingleData<Boolean> combine(SingleData<Boolean> firstContainer, SingleData<Boolean> secondContainer) {
                if (firstContainer.first || secondContainer.first) {
                    firstContainer.first = true;
                }
                return firstContainer;
            }

            @Override
            public Boolean finish(SingleData<Boolean> container) {
                return container.first;
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
        return Optional.ofNullable(collect(new InternalStreamableCollector.First<>()));
    }

    default Optional<T> findAny() {
        return Optional.ofNullable(((InternalStreamable) this).setNext(new FindAnyFinish()).evaluate());
    }

    default Optional<T> findLast() {
        return Optional.ofNullable(collect(new InternalStreamableCollector.Last<>()));
    }
}
