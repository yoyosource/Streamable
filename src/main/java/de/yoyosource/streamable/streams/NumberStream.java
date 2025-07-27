package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.StreamableGatherer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface NumberStream<T extends Number & Comparable<T>> extends Streamable<NumberStream<T>, T> {

    /**
     * Returns a {@code Class} instance with the generic type of {@code NumberStream} for {@link #as(Class)} method.
     *
     * @param <T> the type of elements inside the {@code NumberStream}
     * @return the type for {@link #as(Class)}
     */
    static <T extends Number & Comparable<T>> Class<NumberStream<T>> NumberStream() {
        return (Class<NumberStream<T>>) (Class) NumberStream.class;
    }

    /**
     * Returns a {@code Class} instance with the generic type of {@code NumberStream} for {@link #as(Class)} method.
     *
     * @param <T> the type of elements inside the {@code NumberStream}
     * @param clazz the type {@code T} should be
     * @return the type for {@link #as(Class)}
     */
    static <T extends Number & Comparable<T>> Class<NumberStream<T>> NumberStream(Class<T> clazz) {
        return (Class<NumberStream<T>>) (Class) NumberStream.class;
    }

    /**
     * Returns a stream consisting of the elements of this stream, sorted
     * according to the natural ordering.
     *
     * <p>For ordered streams, the sort is stable.  For unordered streams, no
     * stability guarantees are made.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @return the new stream
     */
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

    /**
     * Returns the median of elements in this stream.  This is a special case of
     * a <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return the median of elements in this stream
     */
    default Optional<T> median() {
        // TODO: Improve the perfomance of this to not create a huge List on near infinite Streams
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

    /**
     * Returns the modus of elements in this stream.  This is a special case of
     * a <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return the modus of elements in this stream
     */
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

    /**
     * Returns the count of elements in this stream.  This is a special case of
     * a <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return the count of elements in this stream
     */
    default long count() {
        return summaryStatistics().getCount();
    }

    /**
     * Returns the sum of elements in this stream.  This is a special case of
     * a <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return the sum of elements in this stream
     */
    default Optional<T> sum() {
        return summaryStatistics().getSum();
    }

    /**
     * Returns the average of elements in this stream.  This is a special case of
     * a <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return the average of elements in this stream
     */
    default Optional<T> average() {
        return summaryStatistics().getAverage();
    }

    /**
     * Returns the minimum element of this stream according to the natural
     * order of the elements.  This is a special case of a
     * <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return an {@code Optional} describing the minimum element of this stream,
     * or an empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the minimum element is null
     */
    default Optional<T> min() {
        return summaryStatistics().getMin();
    }

    /**
     * Returns the maximum element of this stream according to the natural
     * order of the elements.  This is a special case of a
     * <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return an {@code Optional} describing the maximum element of this stream,
     * or an empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the maximum element is null
     */
    default Optional<T> max() {
        return summaryStatistics().getMax();
    }

    /**
     * Returns an {@code SummaryStatistics} describing various
     * summary data about the elements of this stream.  This is a special
     * case of a <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return an {@code SummaryStatistics} describing various summary data
     * about the elements of this stream
     */
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
                    default -> container.sum = element;
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
                    default -> container.min = element;
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
                    default -> container.max = element;
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
                    default -> null;
                };
            }

            @Override
            public SummaryStatistics<T> finish(SummaryStatistics<T> container) {
                average(container);
                return container;
            }
        });
    }

    /**
     * A state object for collecting statistics such as count, min, max, sum, and
     * average.
     *
     * @implNote This implementation is not thread safe.
     *
     * <p>This implementation does not check for overflow of the count or the sum.
     */
    @AllArgsConstructor
    class SummaryStatistics<T extends Number> {
        @Getter
        private long count;

        private T sum;
        private T average;
        private T min;
        private T max;

        /**
         * Returns the sum of values recorded, or zero if no values have been
         * recorded.
         *
         * @return the sum of values, or {@link Optional#empty()} if none
         */
        public Optional<T> getSum() {
            return Optional.ofNullable(sum);
        }

        /**
         * Returns the average value recorded, or zero if no values have
         * been recorded.
         *
         * @return the average value, or {@link Optional#empty()} if none
         */
        public Optional<T> getAverage() {
            return Optional.ofNullable(average);
        }

        /**
         * Returns the min value recorded, or zero if no values have been
         * recorded.
         *
         * @return the min value, or {@link Optional#empty()} if none
         */
        public Optional<T> getMin() {
            return Optional.ofNullable(min);
        }

        /**
         * Returns the max value recorded, or zero if no values have been
         * recorded.
         *
         * @return the max value, or {@link Optional#empty()} if none
         */
        public Optional<T> getMax() {
            return Optional.ofNullable(max);
        }

        @Override
        public String toString() {
            StringBuilder st = new StringBuilder();
            st.append("SummaryStatistics{");
            st.append("count=").append(count);
            if (sum != null) st.append(", sum=").append(sum);
            if (average != null) st.append(", average=").append(average);
            if (min != null) st.append(", min=").append(min);
            if (max != null) st.append(", max=").append(max);
            st.append("}");
            return st.toString();
        }
    }
}
