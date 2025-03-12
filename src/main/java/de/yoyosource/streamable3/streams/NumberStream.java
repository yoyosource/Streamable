package de.yoyosource.streamable3.streams;

import de.yoyosource.streamable3.Streamable;
import de.yoyosource.streamable3.StreamableCollector;
import de.yoyosource.streamable3.StreamableGatherer;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

public interface NumberStream<T extends Number & Comparable<T>> extends Streamable<NumberStream<T>, T> {

    static <T extends Number & Comparable<T>> Class<NumberStream<T>> NumberStream() {
        return (Class<NumberStream<T>>) (Class) NumberStream.class;
    }

    static <T extends Number & Comparable<T>> Class<NumberStream<T>> NumberStream(Class<T> clazz) {
        return (Class<NumberStream<T>>) (Class) NumberStream.class;
    }

    default NumberStream<T> sorted() {
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
                container.sort(null);
                next.accept(container);
            }
        });
    }

    default Optional<T> median() {
        List<T> list = as(JavaStream.JavaStream()).toList();
        if (list.isEmpty()) return Optional.empty();
        T value = list.get(list.size() / 2);
        if (list.size() % 2 == 0) {
            T otherValue = list.get(list.size() / 2 - 1);

            return Optional.of((T) switch (value) {
                case Byte b -> (b + (Byte) otherValue) / 2;
                case Short i -> (i + (Short) otherValue) / 2;
                case Integer i -> (i + (Integer) otherValue) / 2;
                case Long l -> (l + (Long) otherValue) / 2;
                case Float v -> (v + (Float) otherValue) / 2;
                case Double v -> (v + (Double) otherValue) / 2;
                case BigDecimal bigDecimal -> bigDecimal.add((BigDecimal) otherValue).divide(BigDecimal.TWO);
                case BigInteger bigInteger -> bigInteger.add((BigInteger) otherValue).divide(BigInteger.TWO);
                default -> {
                    throw new IllegalStateException("Unknown Number Type");
                }
            });
        } else {
            return Optional.of(value);
        }
    }

    default Optional<T> modus() {
        return collect(new StreamableCollector<T, Map<T, Long>, Optional<T>>() {
            @Override
            public Map<T, Long> container() {
                return new HashMap<>();
            }

            @Override
            public boolean accumulate(Map<T, Long> container, long index, T element) {
                container.compute(element, (k, v) -> v == null ? 1 : v + 1);
                return false;
            }

            @Override
            public Map<T, Long> combine(Map<T, Long> firstContainer, Map<T, Long> secondContainer) {
                secondContainer.forEach((t, aLong) -> {
                    firstContainer.compute(t, (k, v) -> v == null ? aLong : v + aLong);
                });
                return firstContainer;
            }

            @Override
            public Optional<T> finish(Map<T, Long> container) {
                return container.entrySet()
                        .stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey);
            }
        });
    }

    default long count() {
        return summaryStatistics().getCount();
    }

    default Optional<T> sum() {
        return summaryStatistics().getSum();
    }

    default Optional<T> average() {
        return summaryStatistics().getAverage();
    }

    default Optional<T> min() {
        return summaryStatistics().getMin();
    }

    default Optional<T> max() {
        return summaryStatistics().getMax();
    }

    default SummaryStatistics<T> summaryStatistics() {
        return collect(new StreamableCollector<T, SummaryStatistics<T>, SummaryStatistics<T>>() {
            @Override
            public SummaryStatistics<T> container() {
                return new SummaryStatistics<>(0, null, null, null, null);
            }

            @Override
            public boolean accumulate(SummaryStatistics<T> container, long index, T element) {
                add(container, element);
                min(container, element);
                max(container, element);
                container.count++;
                return false;
            }

            @Override
            public SummaryStatistics<T> combine(SummaryStatistics<T> firstContainer, SummaryStatistics<T> secondContainer) {
                add(firstContainer, secondContainer.sum);
                min(firstContainer, secondContainer.min);
                max(firstContainer, secondContainer.max);
                firstContainer.count += secondContainer.count;
                return firstContainer;
            }

            private void add(SummaryStatistics<T> container, T element) {
                switch (container.sum) {
                    case null -> container.sum = element;
                    case Byte b -> container.sum = (T) (Object) (b + (Byte) element);
                    case Short i -> container.sum = (T) (Object) (i + (Short) element);
                    case Integer i -> container.sum = (T) (Object) (i + (Integer) element);
                    case Long l -> container.sum = (T) (Object) (l + (Long) element);
                    case Float v -> container.sum = (T) (Object) (v + (Float) element);
                    case Double v -> container.sum = (T) (Object) (v + (Double) element);
                    case BigDecimal bigDecimal -> container.sum = (T) (bigDecimal.add((BigDecimal) element));
                    case BigInteger bigInteger -> container.sum = (T) (bigInteger.add((BigInteger) element));
                    default -> {
                    }
                }
            }

            private void min(SummaryStatistics<T> container, T element) {
                switch (container.min) {
                    case null -> container.min = element;
                    case Byte b -> container.min = b > (Byte) element ? element : container.min;
                    case Short i -> container.min = i > (Short) element ? element : container.min;
                    case Integer i -> container.min = i > (Integer) element ? element : container.min;
                    case Long l -> container.min = l > (Long) element ? element : container.min;
                    case Float v -> container.min = v > (Float) element ? element : container.min;
                    case Double v -> container.min = v > (Double) element ? element : container.min;
                    case BigDecimal bigDecimal -> container.min = bigDecimal.compareTo((BigDecimal) element) > 0 ? element : container.min;
                    case BigInteger bigInteger -> container.min = bigInteger.compareTo((BigInteger) element) > 0 ? element : container.min;
                    default -> {
                    }
                }
            }

            private void max(SummaryStatistics<T> container, T element) {
                switch (container.max) {
                    case null -> container.max = element;
                    case Byte b -> container.max = b < (Byte) element ? element : container.max;
                    case Short i -> container.max = i < (Short) element ? element : container.max;
                    case Integer i -> container.max = i < (Integer) element ? element : container.max;
                    case Long l -> container.max = l < (Long) element ? element : container.max;
                    case Float v -> container.max = v < (Float) element ? element : container.max;
                    case Double v -> container.max = v < (Double) element ? element : container.max;
                    case BigDecimal bigDecimal -> container.max = bigDecimal.compareTo((BigDecimal) element) < 0 ? element : container.max;
                    case BigInteger bigInteger -> container.max = bigInteger.compareTo((BigInteger) element) < 0 ? element : container.max;
                    default -> {
                    }
                }
            }

            private void average(SummaryStatistics<T> container) {
                container.average = (T) switch (container.sum) {
                    case null -> null;
                    case Byte b -> b / (byte) container.count;
                    case Short i -> i / (short) container.count;
                    case Integer i -> i / (int) container.count;
                    case Long l -> l / container.count;
                    case Float v -> v / (float) container.count;
                    case Double v -> v / (double) container.count;
                    case BigDecimal bigDecimal -> bigDecimal.divide(BigDecimal.valueOf(container.count));
                    case BigInteger bigInteger -> bigInteger.divide(BigInteger.valueOf(container.count));
                    default -> {
                        throw new IllegalStateException("Unknown Number Type");
                    }
                };
            }

            @Override
            public SummaryStatistics<T> finish(SummaryStatistics<T> container) {
                average(container);
                return container;
            }
        });
    }

    @ToString
    @AllArgsConstructor
    class SummaryStatistics<T extends Number> {
        private long count;
        private T sum;
        private T average;
        private T min;
        private T max;

        public long getCount() {
            return count;
        }

        public Optional<T> getSum() {
            return Optional.ofNullable(sum);
        }

        public Optional<T> getAverage() {
            return Optional.ofNullable(average);
        }

        public Optional<T> getMin() {
            return Optional.ofNullable(min);
        }

        public Optional<T> getMax() {
            return Optional.ofNullable(max);
        }
    }
}
