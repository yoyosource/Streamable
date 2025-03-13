package de.yoyosource.streamable3.streams;

import de.yoyosource.streamable.impl.ZippedStream;
import de.yoyosource.streamable3.Streamable;
import de.yoyosource.streamable3.StreamableCollector;
import de.yoyosource.streamable3.StreamableGatherer;
import de.yoyosource.streamable3.internal.InternalStreamable;
import de.yoyosource.streamable3.internal.step.SequentialStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public interface AdvancedStream<T> extends Streamable<AdvancedStream<T>, T> {

    static <T> Class<AdvancedStream<T>> AdvancedStream() {
        return (Class<AdvancedStream<T>>) (Class) AdvancedStream.class;
    }

    static <T> Class<AdvancedStream<T>> AdvancedStream(Class<T> clazz) {
        return (Class<AdvancedStream<T>>) (Class) AdvancedStream.class;
    }

    default AdvancedStream<T> filterIndexed(BiPredicate<T, Long> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                if (predicate.test(element, index)) {
                    next.accept(element);
                }
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    default <R> AdvancedStream<R> mapIndexed(BiFunction<? super T, Long, ? extends R> mapper) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T element, Consumer<? super R> next) {
                next.accept(mapper.apply(element, index));
                return false;
            }

            @Override
            public void finish(Consumer<? super R> next) {
            }
        });
    }

    default <R> AdvancedStream<R> flapMapIndexed(BiFunction<? super T, Long, ? extends Iterable<? extends R>> mapper) {
        return flatGather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T input, Consumer<? super Iterable<R>> next) {
                next.accept((Iterable<R>) mapper.apply(input, index));
                return false;
            }

            @Override
            public void finish(Consumer<? super Iterable<R>> next) {
            }
        });
    }

    @FunctionalInterface
    interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    default <R> AdvancedStream<R> mapMultiIndexed(TriConsumer<? super T, Long, Consumer<? super R>> mapper) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T element, Consumer<? super R> next) {
                mapper.accept(element, index, next);
                return false;
            }

            @Override
            public void finish(Consumer<? super R> next) {
            }
        });
    }

    default AdvancedStream<T> distinctBy(Function<? super T, ?> keyExtractor) {
        return gather(new StreamableGatherer.Simple<>() {
            private Set<Object> elements = new HashSet<>();

            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                if (elements.add(keyExtractor.apply(element))) {
                    next.accept(element);
                }
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    default AdvancedStream<T> peekIndexed(BiConsumer<? super T, Long> action) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                action.accept(element, index);
                next.accept(element);
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    default AdvancedStream<T> takeWhileIndexed(BiPredicate<? super T, Long> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T input, Consumer<? super T> next) {
                if (predicate.test(input, index)) {
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
    default AdvancedStream<T> dropWhileIndexed(BiPredicate<? super T, Long> predicate) {
        return (AdvancedStream<T>) ((InternalStreamable) this).setNext(new SequentialStep(new StreamableGatherer.Simple<T, T>() {
            private boolean take = false;

            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                if (predicate.test(element, index)) take = true;
                if (take) next.accept(element);
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {

            }
        }));
    }

    default AdvancedStream<Map<T, List<T>>> group() {
        return groupBy(Function.identity());
    }

    default <K> AdvancedStream<Map<K, List<T>>> groupBy(Function<? super T, ? extends K> keyExtractor) {
        return gather(new StreamableGatherer<T, Map<K, List<T>>, Map<K, List<T>>>() {
            @Override
            public Map<K, List<T>> container() {
                return new HashMap<>();
            }

            @Override
            public boolean integrate(Map<K, List<T>> container, long index, T element, Consumer<? super Map<K, List<T>>> next) {
                container.computeIfAbsent(keyExtractor.apply(element), __ -> new ArrayList<>()).add(element);
                return false;
            }

            @Override
            public Map<K, List<T>> combine(Map<K, List<T>> firstContainer, Map<K, List<T>> secondContainer) {
                secondContainer.forEach((k, ts) -> {
                    if (firstContainer.containsKey(k)) {
                        firstContainer.get(k).addAll(ts);
                    } else {
                        firstContainer.put(k, ts);
                    }
                });
                return firstContainer;
            }

            @Override
            public void finish(Map<K, List<T>> container, Consumer<? super Map<K, List<T>>> next) {
                next.accept(container);
            }
        });
    }

    default AdvancedStream<T> elementCount(Consumer<Long> consumer) {
        return gather(new StreamableGatherer.Simple<>() {
            private long count = 0;

            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                count++;
                next.accept(element);
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }

            @Override
            public void onClose() {
                consumer.accept(count);
            }
        });
    }

    default AdvancedStream<Map<T, Long>> count() {
        return countBy(Function.identity());
    }

    default <K> AdvancedStream<Map<K, Long>> countBy(Function<? super T, ? extends K> keyExtractor) {
        return gather(new StreamableGatherer<T, Map<K, Long>, Map<K, Long>>() {
            @Override
            public Map<K, Long> container() {
                return new HashMap<>();
            }

            @Override
            public boolean integrate(Map<K, Long> container, long index, T element, Consumer<? super Map<K, Long>> next) {
                container.compute(keyExtractor.apply(element), (k, aLong) -> aLong == null ? 1 : aLong + 1);
                return false;
            }

            @Override
            public Map<K, Long> combine(Map<K, Long> firstContainer, Map<K, Long> secondContainer) {
                secondContainer.forEach((k, aLong) -> {
                    firstContainer.compute(k, (k1, aLong1) -> aLong1 == null ? aLong : aLong1 + aLong);
                });
                return firstContainer;
            }

            @Override
            public void finish(Map<K, Long> container, Consumer<? super Map<K, Long>> next) {
                next.accept(container);
            }
        });
    }

    default AdvancedStream<List<T>> windowFixed(int windowSize) {
        return windowFixed(windowSize, false);
    }

    @SuppressWarnings("unchecked")
    default AdvancedStream<List<T>> windowFixed(int windowSize, boolean keepPartial) {
        if (windowSize < 1) {
            throw new IllegalArgumentException("Window size must be at least 1");
        }
        return (AdvancedStream<List<T>>) ((InternalStreamable) this).setNext(new SequentialStep(new StreamableGatherer.Simple<T, List<T>>() {
            private List<T> elements = new ArrayList<>();

            @Override
            public boolean integrate(long index, T element, Consumer<? super List<T>> next) {
                elements.add(element);
                if (elements.size() == windowSize) {
                    next.accept(elements);
                    elements = new ArrayList<>();
                }
                return false;
            }

            @Override
            public void finish(Consumer<? super List<T>> next) {
                if (keepPartial && !elements.isEmpty()) {
                    next.accept(elements);
                }
            }
        }));
    }

    default AdvancedStream<List<T>> windowSliding(int windowSize) {
        return windowSliding(windowSize, false);
    }

    @SuppressWarnings("unchecked")
    default AdvancedStream<List<T>> windowSliding(int windowSize, boolean keepPartial) {
        if (windowSize < 1) {
            throw new IllegalArgumentException("Window size must be at least 1");
        }
        return (AdvancedStream<List<T>>) ((InternalStreamable) this).setNext(new SequentialStep(new StreamableGatherer.Simple<T, List<T>>() {
            private boolean hadOneResult = false;
            private List<T> elements = new ArrayList<>();

            @Override
            public boolean integrate(long index, T element, Consumer<? super List<T>> next) {
                elements.add(element);
                if (elements.size() > windowSize) {
                    elements.remove(0);
                }
                if (elements.size() == windowSize) {
                    next.accept(new ArrayList<>(elements));
                    hadOneResult = true;
                }
                return false;
            }

            @Override
            public void finish(Consumer<? super List<T>> next) {
                if (keepPartial && !hadOneResult && !elements.isEmpty()) {
                    next.accept(elements);
                }
            }
        }));
    }

    default AdvancedStream<T> concat(Streamable<?, T>... others) {
        AdvancedStream<T> advancedStream = Streamable.from(new Iterator<Streamable<?, T>>() {
            private int index = -1;

            @Override
            public boolean hasNext() {
                return index < others.length;
            }

            @Override
            public Streamable<?, T> next() {
                if (index == -1) {
                    index++;
                    return AdvancedStream.this;
                }
                return others[index++];
            }
        }).flatGather(new StreamableGatherer.Simple<Streamable<?, T>, Iterable<T>>() {
            @Override
            public boolean integrate(long index, Streamable<?, T> element, Consumer<? super Iterable<T>> next) {
                next.accept(element);
                return false;
            }

            @Override
            public void finish(Consumer<? super Iterable<T>> next) {
            }
        }).as(AdvancedStream());
        ((InternalStreamable) advancedStream).setMaxParallelTasks(((InternalStreamable) this).getMaxParallelTasks());
        return advancedStream;
    }

    default AdvancedStream<T> flatMapMulti(BiConsumer<? super T, ? super Consumer<? super Iterable<T>>> mapper) {
        return flatGather(new StreamableGatherer.Simple<T, Iterable<T>>() {
            @Override
            public boolean integrate(long index, T element, Consumer<? super Iterable<T>> next) {
                mapper.accept(element, next);
                return false;
            }

            @Override
            public void finish(Consumer<? super Iterable<T>> next) {
            }
        });
    }

    default Set<T> toSet() {
        return collect(new StreamableCollector<T, Set<T>, Set<T>>() {
            @Override
            public Set<T> container() {
                return new HashSet<>();
            }

            @Override
            public boolean accumulate(Set<T> container, long index, T element) {
                container.add(element);
                return false;
            }

            @Override
            public Set<T> combine(Set<T> firstContainer, Set<T> secondContainer) {
                firstContainer.addAll(secondContainer);
                return firstContainer;
            }

            @Override
            public Set<T> finish(Set<T> container) {
                return container;
            }
        });
    }

    @SuppressWarnings("unchecked")
    default AdvancedStream<T> scan(BiFunction<T, T, T> accumulator) {
        return (AdvancedStream<T>) ((InternalStreamable) this).setNext(new SequentialStep(new StreamableGatherer.Simple<T, T>() {
            private boolean first = true;
            private T current;

            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                if (first) {
                    current = element;
                    next.accept(element);
                    first = false;
                } else {
                    current = accumulator.apply(current, element);
                    next.accept(current);
                }
                return false;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        }));
    }

    default AdvancedStream<Map.Entry<T, Long>> consecutiveElementCount() {
        return consecutiveElementCountBy(Objects::equals);
    }

    @SuppressWarnings("unchecked")
    default AdvancedStream<Map.Entry<T, Long>> consecutiveElementCountBy(BiPredicate<? super T, ? super T> equality) {
        return (AdvancedStream<Map.Entry<T, Long>>) ((InternalStreamable) this).setNext(new SequentialStep(new StreamableGatherer.Simple<T, Map.Entry<T, Long>>() {
            private boolean first = true;
            private T element;
            private long count;

            @Override
            public boolean integrate(long index, T element, Consumer<? super Map.Entry<T, Long>> next) {
                if (first) {
                    this.element = element;
                    count = 1;
                    first = false;
                } else {
                    if (equality.test(this.element, element)) {
                        count++;
                    } else {
                        next.accept(Map.entry(this.element, count));
                        this.element = element;
                        count = 1;
                    }
                }
                return false;
            }

            @Override
            public void finish(Consumer<? super Map.Entry<T, Long>> next) {
                if (first) return;
                next.accept(Map.entry(element, count));

                element = null;
                first = true;
                count = 0;
            }
        }));
    }

    default AdvancedStream<List<T>> allElements() {
        return gather(new StreamableGatherer<T, List<T>, List<T>>() {
            @Override
            public List<T> container() {
                return new ArrayList<>();
            }

            @Override
            public boolean integrate(List<T> container, long index, T element, Consumer<? super List<T>> next) {
                container.add(element);
                return false;
            }

            @Override
            public List<T> combine(List<T> firstContainer, List<T> secondContainer) {
                firstContainer.addAll(secondContainer);
                return firstContainer;
            }

            @Override
            public void finish(List<T> container, Consumer<? super List<T>> next) {
                next.accept(container);
            }
        });
    }

    /*
    default <B> ZippedStream<T, B> zip(de.yoyosource.streamable.Streamable<B> streamable) {
        return gather(new de.yoyosource.streamable.StreamableGatherer<>() {
            private Iterator<B> iterator = streamable.iterator();

            @Override
            public boolean apply(T input, Consumer<ZippedStream.Zip<T, B>> next) {
                if (iterator.hasNext()) {
                    next.accept(new ZippedStream.Zip<>(input, iterator.next()));
                } else {
                    next.accept(new ZippedStream.Zip<>(input, null));
                }
                return false;
            }

            @Override
            public void finish(Consumer<ZippedStream.Zip<T, B>> next) {
                iterator.forEachRemaining(b -> {
                    next.accept(new ZippedStream.Zip<>(null, b));
                });
            }
        });
    }
     */
}
