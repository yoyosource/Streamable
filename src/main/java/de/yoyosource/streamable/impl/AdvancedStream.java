package de.yoyosource.streamable.impl;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;

import java.util.*;
import java.util.function.*;

public interface AdvancedStream<T> extends Streamable<T> {

    static <T> Class<AdvancedStream<T>> type() {
        return (Class<AdvancedStream<T>>) (Class) AdvancedStream.class;
    }

    default AdvancedStream<T> filterIndexed(BiPredicate<T, Long> predicate) {
        return gather(new StreamableGatherer<>() {
            private long index = 0;

            @Override
            public boolean apply(T input, Consumer<T> next) {
                if (predicate.test(input, index++)) {
                    next.accept(input);
                }
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        });
    }

    default <R> AdvancedStream<R> mapIndexed(BiFunction<? super T, Long, ? extends R> mapper) {
        return gather(new StreamableGatherer<>() {
            private long index = 0;

            @Override
            public boolean apply(T input, Consumer<R> next) {
                next.accept(mapper.apply(input, index++));
                return false;
            }

            @Override
            public void finish(Consumer<R> next) {

            }
        });
    }

    default <R> AdvancedStream<R> flapMapIndexed(BiFunction<? super T, Long, ? extends Iterable<? extends R>> mapper) {
        return flatGather(new StreamableGatherer<>() {
            private long index = 0;

            @Override
            public boolean apply(T input, Consumer<Iterable<R>> next) {
                next.accept((Iterable<R>) mapper.apply(input, index++));
                return false;
            }

            @Override
            public void finish(Consumer<Iterable<R>> next) {

            }
        });
    }

    interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    default <R> AdvancedStream<R> mapMultiIndexed(TriConsumer<? super T, Long, ? super Consumer<R>> mapper) {
        return gather(new StreamableGatherer<>() {
            private long index = 0;

            @Override
            public boolean apply(T input, Consumer<R> next) {
                mapper.accept(input, index++, next);
                return false;
            }

            @Override
            public void finish(Consumer<R> next) {

            }
        });
    }

    default AdvancedStream<T> distinctBy(Function<? super T, ?> keyExtractor) {
        return gather(new StreamableGatherer<T, T>() {
            private Set<Object> elements = new HashSet<>();

            @Override
            public boolean apply(T input, Consumer<T> next) {
                if (elements.add(keyExtractor.apply(input))) {
                    next.accept(input);
                }
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        });
    }

    default AdvancedStream<T> peekIndexed(BiConsumer<? super T, Long> action) {
        return gather(new StreamableGatherer<>() {
            private long index = 0;

            @Override
            public boolean apply(T input, Consumer<T> next) {
                action.accept(input, index++);
                next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        });
    }

    default AdvancedStream<T> takeWhileIndexed(BiPredicate<? super T, Long> predicate) {
        return gather(new StreamableGatherer<>() {
            private long index = 0;

            @Override
            public boolean apply(T input, Consumer<T> next) {
                if (predicate.test(input, index++)) {
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

    default AdvancedStream<T> dropWhileIndexed(BiPredicate<? super T, Long> predicate) {
        return gather(new StreamableGatherer<>() {
            private long index = 0;
            private boolean take = false;

            @Override
            public boolean apply(T input, Consumer<T> next) {
                if (predicate.test(input, index++)) take = true;
                if (take) next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {

            }
        });
    }

    default AdvancedStream<Map<T, List<T>>> group() {
        return groupBy(Function.identity());
    }

    default <K> AdvancedStream<Map<K, List<T>>> groupBy(Function<? super T, ? extends K> keyExtractor) {
        return gather(new StreamableGatherer<>() {
            private Map<K, List<T>> data = new HashMap<>();

            @Override
            public boolean apply(T input, Consumer<Map<K, List<T>>> next) {
                data.computeIfAbsent(keyExtractor.apply(input), k -> new ArrayList<>()).add(input);
                return false;
            }

            @Override
            public void finish(Consumer<Map<K, List<T>>> next) {
                next.accept(data);
            }
        });
    }

    default AdvancedStream<T> elementCount(Consumer<Long> consumer) {
        return gather(new StreamableGatherer<>() {
            private long count = 0;

            @Override
            public boolean apply(T input, Consumer<T> next) {
                count++;
                next.accept(input);
                return false;
            }

            @Override
            public void finish(Consumer<T> next) {
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
        return gather(new StreamableGatherer<>() {
            private Map<K, Long> data = new HashMap<>();

            @Override
            public boolean apply(T input, Consumer<Map<K, Long>> next) {
                data.compute(keyExtractor.apply(input), (k, v) -> v == null ? 1 : v + 1);
                return false;
            }

            @Override
            public void finish(Consumer<Map<K, Long>> next) {
                next.accept(data);
            }
        });
    }

    default AdvancedStream<List<T>> windowFixed(int windowSize) {
        return windowFixed(windowSize, false);
    }

    default AdvancedStream<List<T>> windowFixed(int windowSize, boolean keepPartial) {
        if (windowSize < 1) {
            throw new IllegalArgumentException("Window size must be at least 1");
        }
        return gather(new StreamableGatherer<>() {
            private List<T> elements = new ArrayList<>();

            @Override
            public boolean apply(T input, Consumer<List<T>> next) {
                elements.add(input);
                if (elements.size() == windowSize) {
                    next.accept(elements);
                    elements = new ArrayList<>();
                }
                return false;
            }

            @Override
            public void finish(Consumer<List<T>> next) {
                if (keepPartial && elements.size() != 0) {
                    next.accept(elements);
                }
            }
        });
    }

    default AdvancedStream<List<T>> windowSliding(int windowSize) {
        return windowSliding(windowSize, false);
    }

    default AdvancedStream<List<T>> windowSliding(int windowSize, boolean keepPartial) {
        if (windowSize < 1) {
            throw new IllegalArgumentException("Window size must be at least 1");
        }
        return gather(new StreamableGatherer<>() {
            private boolean hadOneResult = false;
            private List<T> elements = new ArrayList<>();

            @Override
            public boolean apply(T input, Consumer<List<T>> next) {
                elements.add(input);
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
            public void finish(Consumer<List<T>> next) {
                if (keepPartial && !hadOneResult && elements.size() != 0) {
                    next.accept(elements);
                }
            }
        });
    }
}
