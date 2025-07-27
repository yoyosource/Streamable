package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.Ordering;
import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.SingleData;

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
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public interface JavaStream<T> extends Streamable<JavaStream<T>, T> {

    /**
     * Returns a {@code Class} instance with the generic type of {@code JavaStream} for {@link #as(Class)} method.
     *
     * @param <T> the type of elements inside the {@code JavaStream}
     * @return the type for {@link #as(Class)}
     */
    static <T> Class<JavaStream<T>> JavaStream() {
        return (Class<JavaStream<T>>) (Class) JavaStream.class;
    }

    /**
     * Returns a {@code Class} instance with the generic type of {@code JavaStream} for {@link #as(Class)} method.
     *
     * @param <T>   the type of elements inside the {@code JavaStream}
     * @param clazz the type {@code T} should be
     * @return the type for {@link #as(Class)}
     */
    static <T> Class<JavaStream<T>> JavaStream(Class<T> clazz) {
        return (Class<JavaStream<T>>) (Class) JavaStream.class;
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
     * @see #mapMulti
     */
    default <R> JavaStream<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return flatGather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, T element, Consumer<? super Iterable<R>> next) {
                Iterable<R> iterable = (Iterable<R>) mapper.apply(element);
                if (iterable == null) return false;
                next.accept(iterable);
                return false;
            }

            @Override
            public void finish(Consumer<? super Iterable<R>> next) {
            }
        });
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
     * @apiNote This method is similar to {@link #flatMap flatMap} in that it applies a one-to-many
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
     * @see #flatMap flatMap
     */
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

    /**
     * Returns a stream consisting of the distinct elements (according to
     * {@link Object#equals(Object)}) of this stream.
     *
     * <p>For ordered streams, the selection of distinct elements is stable
     * (for duplicated elements, the element appearing first in the encounter
     * order is preserved.)  For unordered streams, no stability guarantees
     * are made.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @return the new stream
     * @apiNote Preserving stability for {@code distinct()} in parallel pipelines is
     * relatively expensive (requires that the operation act as a full barrier,
     * with substantial buffering overhead), and stability is often not needed.
     * If consistency with encounter order is required, and you are experiencing
     * poor performance or memory utilization with {@code distinct()} in parallel
     * pipelines, switching to sequential execution with {@link #sequential()}
     * may improve performance.
     */
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

    /**
     * Returns a stream consisting of the elements of this stream, sorted
     * according to the provided {@code Comparator}.
     *
     * <p>For ordered streams, the sort is stable.  For unordered streams, no
     * stability guarantees are made.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @param comparator a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                   <a href="package-summary.html#Statelessness">stateless</a>
     *                   {@code Comparator} to be used to compare stream elements
     * @return the new stream
     */
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

    /**
     * Returns a stream consisting of the elements of this stream, truncated
     * to be no longer than {@code maxSize} in length.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * stateful intermediate operation</a>.
     *
     * @param maxSize the number of elements the stream should be limited to
     * @return the new stream
     * @throws IllegalArgumentException if {@code maxSize} is negative
     * @apiNote While {@code limit()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel pipelines,
     * especially for large values of {@code maxSize}, since {@code limit(n)}
     * is constrained to return not just any <em>n</em> elements, but the
     * <em>first n</em> elements in the encounter order. If consistency with
     * encounter order is required, and you are experiencing poor performance
     * or memory utilization with {@code limit()} in parallel pipelines,
     * switching to sequential execution with {@link #sequential()} may
     * improve performance.
     */
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

    /**
     * Returns a stream consisting of the remaining elements of this stream
     * after discarding the first {@code n} elements of the stream.
     * If this stream contains fewer than {@code n} elements then an
     * empty stream will be returned.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @param skip the number of leading elements to skip
     * @return the new stream
     * @throws IllegalArgumentException if {@code n} is negative
     * @apiNote While {@code skip()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel pipelines,
     * especially for large values of {@code n}, since {@code skip(n)}
     * is constrained to skip not just any <em>n</em> elements, but the
     * <em>first n</em> elements in the encounter order. If consistency
     * with encounter order is required, and you are experiencing poor
     * performance or memory utilization with {@code skip()} in parallel
     * pipelines, switching to sequential execution with
     * {@link #sequential()} may improve performance.
     */
    default JavaStream<T> skip(long skip) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip cannot be negative!");
        } else if (skip == 0) {
            return this;
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
     * @apiNote While {@code takeWhile()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel
     * pipelines, since the operation is constrained to return not just any
     * valid prefix, but the longest prefix of elements in the encounter order.
     * If consistency with encounter order is required, and you are experiencing
     * poor performance or memory utilization with {@code takeWhile()} in
     * parallel pipelines, switching to sequential execution with
     * {@link #sequential()} may improve performance.
     */
    default JavaStream<T> takeWhile(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public Ordering ordering() {
                return Ordering.ORDERED;
            }

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

    /**
     * Returns, if this stream is ordered, a stream consisting of the remaining
     * elements of this stream after dropping the longest prefix of elements
     * that match the given predicate.  Otherwise returns, if this stream is
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
     * @apiNote While {@code dropWhile()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel
     * pipelines, since the operation is constrained to return not just any
     * valid prefix, but the longest prefix of elements in the encounter order.
     * If consistency with encounter order is required, and you are experiencing
     * poor performance or memory utilization with {@code dropWhile()} in
     * parallel pipelines, switching to sequential execution with
     * {@link #sequential()} may improve performance.
     */
    @SuppressWarnings("unchecked")
    default JavaStream<T> dropWhile(Predicate<? super T> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            private boolean take = false;

            @Override
            public Ordering ordering() {
                return Ordering.SEQUENTIAL;
            }

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

    /**
     * Returns an array containing the elements of this stream.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return an array, whose {@linkplain Class#getComponentType runtime component
     * type} is {@code Object}, containing the elements of this stream
     */
    default Object[] toArray() {
        return toArray(Object[]::new);
    }

    /**
     * Returns an array containing the elements of this stream, using the
     * provided {@code generator} function to allocate the returned array, as
     * well as any additional arrays that might be required for a partitioned
     * execution or for resizing.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @param <A>       the component type of the resulting array
     * @param generator a function which produces a new array of the desired
     *                  type and the provided length
     * @return an array containing the elements in this stream
     * @throws ArrayStoreException if the runtime type of any element of this
     *                             stream is not assignable to the {@linkplain Class#getComponentType
     *                             runtime component type} of the generated array
     * @apiNote The generator function takes an integer, which is the size of the
     * desired array, and produces an array of the desired size.  This can be
     * concisely expressed with an array constructor reference:
     * <pre>{@code
     *     Person[] men = people.stream()
     *                          .filter(p -> p.getGender() == MALE)
     *                          .toArray(Person[]::new);
     * }</pre>
     */
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

    /**
     * Performs a <a href="package-summary.html#Reduction">reduction</a> on the
     * elements of this stream, using the provided identity value and an
     * <a href="package-summary.html#Associativity">associative</a>
     * accumulation function, and returns the reduced value.  This is equivalent
     * to:
     * <pre>{@code
     *     T result = identity;
     *     for (T element : this stream)
     *         result = accumulator.apply(result, element)
     *     return result;
     * }</pre>
     * <p>
     * but is not constrained to execute sequentially.
     *
     * <p>The {@code identity} value must be an identity for the accumulator
     * function. This means that for all {@code t},
     * {@code accumulator.apply(identity, t)} is equal to {@code t}.
     * The {@code accumulator} function must be an
     * <a href="package-summary.html#Associativity">associative</a> function.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @param identity    the identity value for the accumulating function
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for combining two values
     * @return the result of the reduction
     * @apiNote Sum, min, max, average, and string concatenation are all special
     * cases of reduction. Summing a stream of numbers can be expressed as:
     *
     * <pre>{@code
     *     Integer sum = integers.reduce(0, (a, b) -> a+b);
     * }</pre>
     * <p>
     * or:
     *
     * <pre>{@code
     *     Integer sum = integers.reduce(0, Integer::sum);
     * }</pre>
     *
     * <p>While this may seem a more roundabout way to perform an aggregation
     * compared to simply mutating a running total in a loop, reduction
     * operations parallelize more gracefully, without needing additional
     * synchronization and with greatly reduced risk of data races.
     */
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

    /**
     * Performs a <a href="package-summary.html#Reduction">reduction</a> on the
     * elements of this stream, using an
     * <a href="package-summary.html#Associativity">associative</a> accumulation
     * function, and returns an {@code Optional} describing the reduced value,
     * if any. This is equivalent to:
     * <pre>{@code
     *     boolean foundAny = false;
     *     T result = null;
     *     for (T element : this stream) {
     *         if (!foundAny) {
     *             foundAny = true;
     *             result = element;
     *         }
     *         else
     *             result = accumulator.apply(result, element);
     *     }
     *     return foundAny ? Optional.of(result) : Optional.empty();
     * }</pre>
     * <p>
     * but is not constrained to execute sequentially.
     *
     * <p>The {@code accumulator} function must be an
     * <a href="package-summary.html#Associativity">associative</a> function.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for combining two values
     * @return an {@link Optional} describing the result of the reduction
     * @throws NullPointerException if the result of the reduction is null
     * @see #reduce(Object, BinaryOperator)
     * @see #min(Comparator)
     * @see #max(Comparator)
     */
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

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable
     * reduction</a> operation on the elements of this stream using a
     * {@code Collector}.  A {@code Collector}
     * encapsulates the functions used as arguments to
     * {@link java.util.stream.Stream#collect(Supplier, BiConsumer, BiConsumer)}, allowing for reuse of
     * collection strategies and composition of collect operations such as
     * multiple-level grouping or partitioning.
     *
     * <p>If the stream is parallel, and the {@code Collector}
     * is {@link Collector.Characteristics#CONCURRENT concurrent}, and
     * either the stream is unordered or the collector is
     * {@link Collector.Characteristics#UNORDERED unordered},
     * then a concurrent reduction will be performed (see {@link Collector} for
     * details on concurrent reduction.)
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * <p>When executed in parallel, multiple intermediate results may be
     * instantiated, populated, and merged so as to maintain isolation of
     * mutable data structures.  Therefore, even when executed in parallel
     * with non-thread-safe data structures (such as {@code ArrayList}), no
     * additional synchronization is needed for a parallel reduction.
     *
     * @param <R>       the type of the result
     * @param <A>       the intermediate accumulation type of the {@code Collector}
     * @param collector the {@code Collector} describing the reduction
     * @return the result of the reduction
     * @apiNote The following will accumulate strings into a List:
     * <pre>{@code
     *     List<String> asList = stringStream.collect(Collectors.toList());
     * }</pre>
     *
     * <p>The following will classify {@code Person} objects by city:
     * <pre>{@code
     *     Map<String, List<Person>> peopleByCity
     *         = personStream.collect(Collectors.groupingBy(Person::getCity));
     * }</pre>
     *
     * <p>The following will classify {@code Person} objects by state and city,
     * cascading two {@code Collector}s together:
     * <pre>{@code
     *     Map<String, Map<String, List<Person>>> peopleByStateAndCity
     *         = personStream.collect(Collectors.groupingBy(Person::getState,
     *                                                      Collectors.groupingBy(Person::getCity)));
     * }</pre>
     * @see Collectors
     */
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

    /**
     * Accumulates the elements of this stream into a {@code List}. The elements in
     * the list will be in this stream's encounter order, if one exists. There are no
     * guarantees on the implementation type or serializability of the returned List.
     *
     * <p>The returned instance may be <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
     * Callers should make no assumptions about the identity of the returned instances.
     * Identity-sensitive operations on these instances (reference equality ({@code ==}),
     * identity hash code, and synchronization) are unreliable and should be avoided.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return a List containing the stream elements
     * @apiNote If more control over the returned object is required, use
     * {@link Collectors#toCollection(Supplier)}.
     */
    default List<T> toList() {
        return collect(Collectors.toList());
    }

    /**
     * Returns the minimum element of this stream according to the provided
     * {@code Comparator}.  This is a special case of a
     * <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @param comparator a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                   <a href="package-summary.html#Statelessness">stateless</a>
     *                   {@code Comparator} to compare elements of this stream
     * @return an {@code Optional} describing the minimum element of this stream,
     * or an empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the minimum element is null
     */
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

    /**
     * Returns the maximum element of this stream according to the provided
     * {@code Comparator}.  This is a special case of a
     * <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @param comparator a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                   <a href="package-summary.html#Statelessness">stateless</a>
     *                   {@code Comparator} to compare elements of this stream
     * @return an {@code Optional} describing the maximum element of this stream,
     * or an empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the maximum element is null
     */
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

    /**
     * Returns the count of elements in this stream.  This is a special case of
     * a <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return the count of elements in this stream
     */
    default long count() {
        return collect(new StreamableCollector.Simple<>() {
            private long count = 0;

            @Override
            public Ordering ordering() {
                return Ordering.SEQUENTIAL;
            }

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

    /**
     * Returns whether any elements of this stream match the provided
     * predicate.  May not evaluate the predicate on all elements if not
     * necessary for determining the result.  If the stream is empty then
     * {@code false} is returned and the predicate is not evaluated.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to elements of this stream
     * @return {@code true} if any elements of the stream match the provided
     * predicate, otherwise {@code false}
     * @apiNote This method evaluates the <em>existential quantification</em> of the
     * predicate over the elements of the stream (for some x P(x)).
     */
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

    /**
     * Returns whether all elements of this stream match the provided predicate.
     * May not evaluate the predicate on all elements if not necessary for
     * determining the result.  If the stream is empty then {@code true} is
     * returned and the predicate is not evaluated.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to elements of this stream
     * @return {@code true} if either all elements of the stream match the
     * provided predicate or the stream is empty, otherwise {@code false}
     * @apiNote This method evaluates the <em>universal quantification</em> of the
     * predicate over the elements of the stream (for all x P(x)).  If the
     * stream is empty, the quantification is said to be <em>vacuously
     * satisfied</em> and is always {@code true} (regardless of P(x)).
     */
    default boolean allMatch(Predicate<? super T> predicate) {
        return !anyMatch(predicate.negate());
    }

    /**
     * Returns whether no elements of this stream match the provided predicate.
     * May not evaluate the predicate on all elements if not necessary for
     * determining the result.  If the stream is empty then {@code true} is
     * returned and the predicate is not evaluated.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to elements of this stream
     * @return {@code true} if either no elements of the stream match the
     * provided predicate or the stream is empty, otherwise {@code false}
     * @apiNote This method evaluates the <em>universal quantification</em> of the
     * negated predicate over the elements of the stream (for all x ~P(x)).  If
     * the stream is empty, the quantification is said to be vacuously satisfied
     * and is always {@code true}, regardless of P(x).
     */
    default boolean noneMatch(Predicate<? super T> predicate) {
        return !anyMatch(predicate);
    }

    /**
     * Returns an {@link Optional} describing the first element of this stream,
     * or an empty {@code Optional} if the stream is empty.  If the stream has
     * no encounter order, then any element may be returned.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @return an {@code Optional} describing the first element of this stream,
     * or an empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the element selected is null
     */
    default Optional<T> findFirst() {
        return Optional.ofNullable(collect(new StreamableCollector.Simple<>() {
            @Override
            public Ordering ordering() {
                return Ordering.SEQUENTIAL;
            }

            private T element = null;

            @Override
            public boolean accumulate(long index, T element) {
                this.element = element;
                return true;
            }

            @Override
            public T finish() {
                return element;
            }
        }));
    }

    /**
     * Returns an {@link Optional} describing the last element of this stream,
     * or an empty {@code Optional} if the stream is empty.  If the stream has
     * no encounter order, then any element may be returned.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @return an {@code Optional} describing the last element of this stream,
     * or an empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the element selected is null
     */
    default Optional<T> findAny() {
        return Optional.ofNullable(collect(new StreamableCollector.Simple<>() {
            private T element = null;

            @Override
            public Ordering ordering() {
                return Ordering.UNORDERED;
            }

            @Override
            public boolean accumulate(long index, T element) {
                this.element = element;
                return true;
            }

            @Override
            public T finish() {
                return element;
            }
        }));
    }

    /**
     * Returns an {@link Optional} describing some element of the stream, or an
     * empty {@code Optional} if the stream is empty.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * <p>The behavior of this operation is explicitly nondeterministic; it is
     * free to select any element in the stream.  This is to allow for maximal
     * performance in parallel operations; the cost is that multiple invocations
     * on the same source may not return the same result.  (If a stable result
     * is desired, use {@link #findFirst()} instead.)
     *
     * @return an {@code Optional} describing some element of this stream, or an
     * empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the element selected is null
     * @see #findFirst()
     */
    default Optional<T> findLast() {
        return Optional.ofNullable(collect(new StreamableCollector.Simple<>() {
            private long index = -1;
            private T element = null;

            @Override
            public Ordering ordering() {
                return Ordering.ORDERED;
            }

            @Override
            public boolean accumulate(long index, T element) {
                if (index > this.index) {
                    synchronized (this) {
                        if (index > this.index) {
                            this.index = index;
                            this.element = element;
                        }
                    }
                }
                return false;
            }

            @Override
            public T finish() {
                return element;
            }
        }));
    }
}
