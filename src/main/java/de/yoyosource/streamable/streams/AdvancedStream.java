package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.Evaluation;
import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.InternalStreamable;
import de.yoyosource.streamable.internal.step.FlattenStep;
import de.yoyosource.streamable.internal.step.ZipStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface AdvancedStream<T> extends Streamable<AdvancedStream<T>, T> {

    /**
     * Returns a {@code Class} instance with the generic type of {@code AdvancedStream} for {@link #as(Class)} method.
     *
     * @param <T> the type of elements inside the {@code AdvancedStream}
     * @return the type for {@link #as(Class)}
     */
    static <T> Class<AdvancedStream<T>> AdvancedStream() {
        return (Class<AdvancedStream<T>>) (Class) AdvancedStream.class;
    }

    /**
     * Returns a {@code Class} instance with the generic type of {@code AdvancedStream} for {@link #as(Class)} method.
     *
     * @param <T> the type of elements inside the {@code AdvancedStream}
     * @param clazz the type {@code T} should be
     * @return the type for {@link #as(Class)}
     */
    static <T> Class<AdvancedStream<T>> AdvancedStream(Class<T> clazz) {
        return (Class<AdvancedStream<T>>) (Class) AdvancedStream.class;
    }

    /**
     * Returns a stream consisting of the elements of this stream that match
     * the given predicate.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to each element to determine if it
     *                  should be included
     * @return the new stream
     */
    default AdvancedStream<T> filterIndexed(BiPredicate<T, Long> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.NO_CONTAINER, Evaluation.GREEDY);
            }

            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                if (predicate.test(element, index)) {
                    next.accept(element);
                }
                return true;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    /**
     * Returns a stream consisting of the results of applying the given
     * function to the elements of this stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param <R>    The element type of the new stream
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element
     * @return the new stream
     */
    default <R> AdvancedStream<R> mapIndexed(BiFunction<? super T, Long, ? extends R> mapper) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.NO_CONTAINER, Evaluation.GREEDY);
            }

            @Override
            public boolean integrate(long index, T element, Consumer<? super R> next) {
                next.accept(mapper.apply(element, index));
                return true;
            }

            @Override
            public void finish(Consumer<? super R> next) {
            }
        });
    }

    /**
     * Returns a stream consisting of the results of replacing each element of
     * this stream with the contents of a mapped stream produced by applying
     * the provided mapping function to each element.  Each mapped stream is
     * {@link Streamable#close() closed} after its contents
     * have been placed into this stream.  (If a mapped stream is {@code null}
     * an empty stream is used, instead.)
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param <R>    The element type of the new stream
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element which produces an iterable
     *               of new values
     * @return the new stream
     * @apiNote The {@code flatMap()} operation has the effect of applying a one-to-many
     * transformation to the elements of the stream, and then flattening the
     * resulting elements into a new stream.
     *
     * <p><b>Examples.</b>
     *
     * <p>If {@code orders} is a stream of purchase orders, and each purchase
     * order contains a collection of line items, then the following produces a
     * stream containing all the line items in all the orders:
     * <pre>{@code
     *     orders.flatMap(order -> order.getLineItems().stream())...
     * }</pre>
     *
     * <p>If {@code path} is the path to a file, then the following produces a
     * stream of the {@code words} contained in that file:
     * <pre>{@code
     *     Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8);
     *     Stream<String> words = lines.flatMap(line -> Stream.of(line.split(" +")));
     * }</pre>
     * The {@code mapper} function passed to {@code flatMap} splits a line,
     * using a simple regular expression, into an array of words, and then
     * creates a stream of words from that array.
     * @see #mapMultiIndexed
     */
    default <R> AdvancedStream<R> flatMapIndexed(BiFunction<? super T, Long, ? extends Iterable<? extends R>> mapper) {
        return flatGather(new StreamableGatherer.Simple<>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.NO_CONTAINER, Evaluation.GREEDY);
            }

            @Override
            public boolean integrate(long index, T input, Consumer<? super Iterable<R>> next) {
                next.accept((Iterable<R>) mapper.apply(input, index));
                return true;
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

    /**
     * Returns a stream consisting of the results of replacing each element of
     * this stream with multiple elements, specifically zero or more elements.
     * Replacement is performed by applying the provided mapping function to each
     * element in conjunction with a {@linkplain Consumer consumer} argument
     * that accepts replacement elements. The mapping function calls the consumer
     * zero or more times to provide the replacement elements.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * <p>If the {@linkplain Consumer consumer} argument is used outside the scope of
     * its application to the mapping function, the results are undefined.
     *
     * @param <R>    The element type of the new stream
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function that generates replacement elements
     * @return the new stream
     * @apiNote This method is similar to {@link #flatMapIndexed flatMap} in that it applies a one-to-many
     * transformation to the elements of the stream and flattens the result elements
     * into a new stream. This method is preferable to {@code flatMap} in the following
     * circumstances:
     * <ul>
     * <li>When replacing each stream element with a small (possibly zero) number of
     * elements. Using this method avoids the overhead of creating a new Stream instance
     * for every group of result elements, as required by {@code flatMap}.</li>
     * <li>When it is easier to use an imperative approach for generating result
     * elements than it is to return them in the form of a Stream.</li>
     * </ul>
     *
     * <p>If a lambda expression is provided as the mapper function argument, additional type
     * information may be necessary for proper inference of the element type {@code <R>} of
     * the returned stream. This can be provided in the form of explicit type declarations for
     * the lambda parameters or as an explicit type argument to the {@code mapMulti} call.
     *
     * <p><b>Examples</b>
     *
     * <p>Given a stream of {@code Number} objects, the following
     * produces a list containing only the {@code Integer} objects:
     * <pre>{@code
     *     Stream<Number> numbers = ... ;
     *     List<Integer> integers = numbers.<Integer>mapMulti((number, consumer) -> {
     *             if (number instanceof Integer i)
     *                 consumer.accept(i);
     *         })
     *         .collect(Collectors.toList());
     * }</pre>
     *
     * <p>If we have an {@code Iterable<Object>} and need to recursively expand its elements
     * that are themselves of type {@code Iterable}, we can use {@code mapMulti} as follows:
     * <pre>{@code
     * class C {
     *     static void expandIterable(Object e, Consumer<Object> c) {
     *         if (e instanceof Iterable<?> elements) {
     *             for (Object ie : elements) {
     *                 expandIterable(ie, c);
     *             }
     *         } else if (e != null) {
     *             c.accept(e);
     *         }
     *     }
     *
     *     public static void main(String[] args) {
     *         var nestedList = List.of(1, List.of(2, List.of(3, 4)), 5);
     *         Stream<Object> expandedStream = nestedList.stream().mapMulti(C::expandIterable);
     *     }
     * }
     * }</pre>
     * @see #flatMapIndexed
     */
    default <R> AdvancedStream<R> mapMultiIndexed(TriConsumer<? super T, Long, Consumer<? super R>> mapper) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.NO_CONTAINER, Evaluation.GREEDY);
            }

            @Override
            public boolean integrate(long index, T element, Consumer<? super R> next) {
                mapper.accept(element, index, next);
                return true;
            }

            @Override
            public void finish(Consumer<? super R> next) {
            }
        });
    }

    default AdvancedStream<T> distinctBy(Function<? super T, ?> keyExtractor) {
        return gather(new StreamableGatherer.Simple<>() {
            private Set<Object> elements = Collections.synchronizedSet(new HashSet<>());

            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.NO_CONTAINER, Evaluation.ORDERED, Evaluation.GREEDY);
            }

            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                if (elements.add(keyExtractor.apply(element))) {
                    next.accept(element);
                }
                return true;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    /**
     * Returns a stream consisting of the elements of this stream, additionally
     * performing the provided action on each element as elements are consumed
     * from the resulting stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * <p>For parallel stream pipelines, the action may be called at
     * whatever time and in whatever thread the element is made available by the
     * upstream operation.  If the action modifies shared state,
     * it is responsible for providing the required synchronization.
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *               non-interfering</a> action to perform on the elements as
     *               they are consumed from the stream
     * @return the new stream
     * @apiNote This method exists mainly to support debugging, where you want
     * to see the elements as they flow past a certain point in a pipeline:
     * <pre>{@code
     *     Stream.of("one", "two", "three", "four")
     *         .filter(e -> e.length() > 3)
     *         .peek(e -> System.out.println("Filtered value: " + e))
     *         .map(String::toUpperCase)
     *         .peek(e -> System.out.println("Mapped value: " + e))
     *         .collect(Collectors.toList());
     * }</pre>
     *
     * <p>In cases where the stream implementation is able to optimize away the
     * production of some or all the elements (such as with short-circuiting
     * operations like {@code findFirst}, or in the example described in
     * {@link #count}), the action will not be invoked for those elements.
     */
    default AdvancedStream<T> peekIndexed(BiConsumer<? super T, Long> action) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.NO_CONTAINER, Evaluation.GREEDY);
            }

            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                action.accept(element, index);
                next.accept(element);
                return true;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    /**
     * Returns, if this stream is ordered, a stream consisting of the longest
     * prefix of elements taken from this stream that match the given predicate.
     * Otherwise returns, if this stream is unordered, a stream consisting of a
     * subset of elements taken from this stream that match the given predicate.
     *
     * <p>If this stream is ordered then the longest prefix is a contiguous
     * sequence of elements of this stream that match the given predicate.  The
     * first element of the sequence is the first element of this stream, and
     * the element immediately following the last element of the sequence does
     * not match the given predicate.
     *
     * <p>If this stream is unordered, and some (but not all) elements of this
     * stream match the given predicate, then the behavior of this operation is
     * nondeterministic; it is free to take any subset of matching elements
     * (which includes the empty set).
     *
     * <p>Independent of whether this stream is ordered or unordered if all
     * elements of this stream match the given predicate then this operation
     * takes all elements (the result is the same as the input), or if no
     * elements of the stream match the given predicate then no elements are
     * taken (the result is an empty stream).
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * stateful intermediate operation</a>.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to elements to determine the longest
     *                  prefix of elements.
     * @return the new stream
     * @implSpec The default implementation obtains the {@link #spliterator() spliterator}
     * of this stream, wraps that spliterator so as to support the semantics
     * of this operation on traversal, and returns a new stream associated with
     * the wrapped spliterator.  The returned stream preserves the execution
     * characteristics of this stream (namely parallel or sequential execution
     * as per {@link #isParallel()}) but the wrapped spliterator may choose to
     * not support splitting.  When the returned stream is closed, the close
     * handlers for both the returned and this stream are invoked.
     * @apiNote While {@code takeWhileIndexed()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel
     * pipelines, since the operation is constrained to return not just any
     * valid prefix, but the longest prefix of elements in the encounter order.
     * If consistency with encounter order is required, and you are experiencing
     * poor performance or memory utilization with {@code takeWhileIndexed()} in
     * parallel pipelines, switching to sequential execution with
     * {@link #sequential()} may improve performance.
     */
    default AdvancedStream<T> takeWhileIndexed(BiPredicate<? super T, Long> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.ORDERED, Evaluation.SEQUENTIAL, Evaluation.NO_CONTAINER);
            }

            @Override
            public boolean integrate(long index, T input, Consumer<? super T> next) {
                if (predicate.test(input, index)) {
                    next.accept(input);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    /**
     * Returns, if this stream is ordered, a stream consisting of the remaining
     * elements of this stream after dropping the longest prefix of elements
     * that match the given predicate. Otherwise, returns, if this stream is
     * unordered, a stream consisting of the remaining elements of this stream
     * after dropping a subset of elements that match the given predicate.
     *
     * <p>If this stream is ordered then the longest prefix is a contiguous
     * sequence of elements of this stream that match the given predicate.  The
     * first element of the sequence is the first element of this stream, and
     * the element immediately following the last element of the sequence does
     * not match the given predicate.
     *
     * <p>If this stream is unordered, and some (but not all) elements of this
     * stream match the given predicate, then the behavior of this operation is
     * nondeterministic; it is free to drop any subset of matching elements
     * (which includes the empty set).
     *
     * <p>Independent of whether this stream is ordered or unordered if all
     * elements of this stream match the given predicate then this operation
     * drops all elements (the result is an empty stream), or if no elements of
     * the stream match the given predicate then no elements are dropped (the
     * result is the same as the input).
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to elements to determine the longest
     *                  prefix of elements.
     * @return the new stream
     * @implSpec The default implementation obtains the {@link #spliterator() spliterator}
     * of this stream, wraps that spliterator so as to support the semantics
     * of this operation on traversal, and returns a new stream associated with
     * the wrapped spliterator.  The returned stream preserves the execution
     * characteristics of this stream (namely parallel or sequential execution
     * as per {@link #isParallel()}) but the wrapped spliterator may choose to
     * not support splitting.  When the returned stream is closed, the close
     * handlers for both the returned and this stream are invoked.
     * @apiNote While {@code dropWhileIndexed()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel
     * pipelines, since the operation is constrained to return not just any
     * valid prefix, but the longest prefix of elements in the encounter order.
     * If consistency with encounter order is required, and you are experiencing
     * poor performance or memory utilization with {@code dropWhileIndexed()} in
     * parallel pipelines, switching to sequential execution with
     * {@link #sequential()} may improve performance.
     */
    @SuppressWarnings("unchecked")
    default AdvancedStream<T> dropWhileIndexed(BiPredicate<? super T, Long> predicate) {
        return gather(new StreamableGatherer.Simple<T, T>() {
            private boolean take = false;

            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.ORDERED, Evaluation.SEQUENTIAL, Evaluation.NO_CONTAINER);
            }

            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                if (!predicate.test(element, index)) take = true;
                if (take) next.accept(element);
                return true;
            }

            @Override
            public void finish(Consumer<? super T> next) {

            }
        });
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
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.ORDERED, Evaluation.GREEDY, Evaluation.NO_CONTAINER);
            }

            @Override
            public boolean integrate(Map<K, List<T>> container, long index, T element, Consumer<? super Map<K, List<T>>> next) {
                System.out.println("Integrating: " + container + " with " + element);
                container.computeIfAbsent(keyExtractor.apply(element), __ -> new ArrayList<>()).add(element);
                return true;
            }

            @Override
            public Map<K, List<T>> combine(Map<K, List<T>> firstContainer, Map<K, List<T>> secondContainer) {
                System.out.println("Combining: " + firstContainer + " + " + secondContainer);
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
            private AtomicLong count = new AtomicLong();

            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.NO_CONTAINER, Evaluation.GREEDY);
            }

            @Override
            public boolean integrate(long index, T element, Consumer<? super T> next) {
                count.incrementAndGet();
                next.accept(element);
                return true;
            }

            @Override
            public void finish(Consumer<? super T> next) {
                consumer.accept(count.get());
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
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.GREEDY);
            }

            @Override
            public boolean integrate(Map<K, Long> container, long index, T element, Consumer<? super Map<K, Long>> next) {
                container.compute(keyExtractor.apply(element), (k, aLong) -> aLong == null ? 1 : aLong + 1);
                return true;
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
        return gather(new StreamableGatherer.Simple<>() {
            private List<T> elements = new ArrayList<>();

            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.ORDERED, Evaluation.SEQUENTIAL, Evaluation.NO_CONTAINER, Evaluation.GREEDY);
            }

            @Override
            public boolean integrate(long index, T element, Consumer<? super List<T>> next) {
                elements.add(element);
                if (elements.size() == windowSize) {
                    next.accept(elements);
                    elements = new ArrayList<>();
                }
                return true;
            }

            @Override
            public void finish(Consumer<? super List<T>> next) {
                if (keepPartial && !elements.isEmpty()) {
                    next.accept(elements);
                }
            }
        });
    }

    default AdvancedStream<List<T>> windowSliding(int windowSize) {
        return windowSliding(windowSize, false);
    }

    @SuppressWarnings("unchecked")
    default AdvancedStream<List<T>> windowSliding(int windowSize, boolean keepPartial) {
        if (windowSize < 1) {
            throw new IllegalArgumentException("Window size must be at least 1");
        }
        return gather(new StreamableGatherer.Simple<>() {
            private boolean hadOneResult = false;
            private List<T> elements = new ArrayList<>();

            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.ORDERED, Evaluation.SEQUENTIAL, Evaluation.NO_CONTAINER, Evaluation.GREEDY);
            }

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
                return true;
            }

            @Override
            public void finish(Consumer<? super List<T>> next) {
                if (keepPartial && !hadOneResult && !elements.isEmpty()) {
                    next.accept(elements);
                }
            }
        });
    }

    /**
     * Creates a lazily concatenated stream whose elements are all the
     * elements of the first stream followed by all the elements of the
     * second stream.  The resulting stream is ordered if both
     * of the input streams are ordered, and parallel if either of the input
     * streams is parallel.  When the resulting stream is closed, the close
     * handlers for both input streams are invoked.
     *
     * <p>This method operates on at least two input streams and binds each stream
     * to its source.  As a result subsequent modifications to an input stream
     * source may not be reflected in the concatenated stream result.
     *
     * @implNote
     * Use caution when constructing streams from repeated concatenation.
     * Accessing an element of a deeply concatenated stream can result in deep
     * call chains, or even {@code StackOverflowError}.
     *
     * <p>Subsequent changes to the sequential/parallel execution mode of the
     * returned stream are not guaranteed to be propagated to the input streams.
     *
     * @param others all Streams to append to the current
     * @return the concatenation of this stream with all the inputted streams
     */
    @SuppressWarnings("unchecked")
    default AdvancedStream<T> concat(Streamable<?, T>... others) {
        AdvancedStream<T> advancedStream = (AdvancedStream<T>) Streamable.from(new Iterator<Streamable<?, T>>() {
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
        }).as(AdvancedStream());
        advancedStream = (AdvancedStream<T>) ((InternalStreamable) advancedStream).setNext(new FlattenStep());
        ((InternalStreamable) advancedStream).setMaxParallelTasks(((InternalStreamable) this).getMaxParallelTasks());
        return advancedStream;
    }

    /**
     * Returns a stream consisting of the results of replacing each element of
     * this stream with multiple elements, specifically zero or more elements.
     * Replacement is performed by applying the provided mapping function to each
     * element in conjunction with a {@linkplain Consumer consumer} argument
     * that accepts replacement elements. The mapping function calls the consumer
     * zero or more times to provide the replacement elements.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * <p>If the {@linkplain Consumer consumer} argument is used outside the scope of
     * its application to the mapping function, the results are undefined.
     *
     * @param <R>    The element type of the new stream
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function that generates replacement elements
     * @return the new stream
     * @apiNote This method is similar to {@link JavaStream#flatMap flatMap} in that it applies a one-to-many
     * transformation to the elements of the stream and flattens the result elements
     * into a new stream. This method is preferable to {@code flatMap} in the following
     * circumstances:
     * <ul>
     * <li>When replacing each stream element with a small (possibly zero) number of
     * elements. Using this method avoids the overhead of creating a new Stream instance
     * for every group of result elements, as required by {@code flatMap}.</li>
     * <li>When it is easier to use an imperative approach for generating result
     * elements than it is to return them in the form of a Stream.</li>
     * </ul>
     *
     * <p>If a lambda expression is provided as the mapper function argument, additional type
     * information may be necessary for proper inference of the element type {@code <R>} of
     * the returned stream. This can be provided in the form of explicit type declarations for
     * the lambda parameters or as an explicit type argument to the {@code mapMulti} call.
     *
     * <p><b>Examples</b>
     *
     * <p>Given a stream of {@code Number} objects, the following
     * produces a list containing only the {@code Integer} objects:
     * <pre>{@code
     *     Stream<Number> numbers = ... ;
     *     List<Integer> integers = numbers.<Integer>mapMulti((number, consumer) -> {
     *             if (number instanceof Integer i)
     *                 consumer.accept(i);
     *         })
     *         .collect(Collectors.toList());
     * }</pre>
     *
     * <p>If we have an {@code Iterable<Object>} and need to recursively expand its elements
     * that are themselves of type {@code Iterable}, we can use {@code mapMulti} as follows:
     * <pre>{@code
     * class C {
     *     static void expandIterable(Object e, Consumer<Object> c) {
     *         if (e instanceof Iterable<?> elements) {
     *             for (Object ie : elements) {
     *                 expandIterable(ie, c);
     *             }
     *         } else if (e != null) {
     *             c.accept(e);
     *         }
     *     }
     *
     *     public static void main(String[] args) {
     *         var nestedList = List.of(1, List.of(2, List.of(3, 4)), 5);
     *         Stream<Object> expandedStream = nestedList.stream().mapMulti(C::expandIterable);
     *     }
     * }
     * }</pre>
     * @see JavaStream#mapMulti
     * @see JavaStream#flatMap
     */
    default <R> AdvancedStream<R> flatMapMulti(BiConsumer<? super T, ? super Consumer<? super Iterable<R>>> mapper) {
        return flatGather(new StreamableGatherer.Simple<>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.NO_CONTAINER, Evaluation.GREEDY);
            }

            @Override
            public boolean integrate(long index, T element, Consumer<? super Iterable<R>> next) {
                mapper.accept(element, next);
                return true;
            }

            @Override
            public void finish(Consumer<? super Iterable<R>> next) {
            }
        });
    }

    /**
     * Accumulates the elements of this stream into a {@code Set}. The elements in
     * the set will be in this stream's encounter order, if one exists. There are no
     * guarantees on the implementation type or serializability of the returned List.
     *
     * <p>The returned instance may be <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
     * Callers should make no assumptions about the identity of the returned instances.
     * Identity-sensitive operations on these instances (reference equality ({@code ==}),
     * identity hash code, and synchronization) are unreliable and should be avoided.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return a Set containing the stream elements
     * @apiNote If more control over the returned object is required, use
     * {@link Collectors#toCollection(Supplier)}.
     */
    default Set<T> toSet() {
        return collect(new StreamableCollector<T, Set<T>, Set<T>>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.GREEDY);
            }

            @Override
            public Set<T> container() {
                return new HashSet<>();
            }

            @Override
            public boolean accumulate(Set<T> container, long index, T element) {
                container.add(element);
                return true;
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
        return gather(new StreamableGatherer.Simple<>() {
            private boolean first = true;
            private T current;

            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.ORDERED, Evaluation.SEQUENTIAL, Evaluation.NO_CONTAINER);
            }

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
                return true;
            }

            @Override
            public void finish(Consumer<? super T> next) {
            }
        });
    }

    default AdvancedStream<Map.Entry<T, Long>> consecutiveElementCount() {
        return consecutiveElementCountBy(Objects::equals);
    }

    @SuppressWarnings("unchecked")
    default AdvancedStream<Map.Entry<T, Long>> consecutiveElementCountBy(BiPredicate<? super T, ? super T> equality) {
        return gather(new StreamableGatherer.Simple<>() {
            private boolean first = true;
            private T element;
            private long count;

            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.ORDERED, Evaluation.SEQUENTIAL, Evaluation.NO_CONTAINER);
            }

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
                return true;
            }

            @Override
            public void finish(Consumer<? super Map.Entry<T, Long>> next) {
                if (first) return;
                next.accept(Map.entry(element, count));

                element = null;
                first = true;
                count = 0;
            }
        });
    }

    default AdvancedStream<List<T>> allElements() {
        return gather(new StreamableGatherer<T, List<T>, List<T>>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.GREEDY);
            }

            @Override
            public List<T> container() {
                return new ArrayList<>();
            }

            @Override
            public boolean integrate(List<T> container, long index, T element, Consumer<? super List<T>> next) {
                container.add(element);
                return true;
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

    default <B> ZippedStream<T, B> zip(Streamable<?, B> streamable) {
        return zip(streamable, false);
    }

    default <B> ZippedStream<T, B> zip(Streamable<?, B> streamable, boolean ignoreNulls) {
        return ((Streamable<?, ZippedStream.Zip<?, ?>>) ((InternalStreamable) this).setNext(new ZipStep(streamable, ignoreNulls)))
                .as(ZippedStream.class);
    }
}
