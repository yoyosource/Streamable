package de.yoyosource.streamable;

import de.yoyosource.streamable.internal.InternalStreamable;
import de.yoyosource.streamable.internal.step.OnCloseStep;
import de.yoyosource.streamable.streams.JavaStream;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static de.yoyosource.streamable.streams.JavaStream.JavaStream;

public interface Streamable<S extends Streamable<S, T>, T> extends Iterable<T>, AutoCloseable {

    /**
     * Returns an empty {@code JavaStream}.
     *
     * @param <T> the type of stream elements
     * @return an empty stream
     */
    static <T> JavaStream<T> empty() {
        return StreamableManager.from(Collections.<T>emptyIterator())
                .as(JavaStream());
    }

    /**
     * Returns a {@code JavaStream} containing a single element.
     *
     * @param element the single element
     * @param <T>     the type of stream elements
     * @return a singleton stream
     */
    static <T> JavaStream<T> of(T element) {
        return StreamableManager.from(Collections.singletonList(element).iterator())
                .as(JavaStream());
    }

    /**
     * Returns a {@code JavaStream} containing a single element, if
     * non-null, otherwise returns an empty {@code JavaStream}.
     *
     * @param element the single element
     * @param <T>     the type of stream elements
     * @return a stream with a single element if the specified element
     * is non-null, otherwise an empty stream
     */
    static <T> JavaStream<T> ofNullable(T element) {
        return element == null ? empty() : of(element);
    }

    /**
     * Returns a stream whose elements are the specified values.
     *
     * @param <T>      the type of stream elements
     * @param elements the elements of the new stream
     * @return the new stream
     */
    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a stream from an array is safe
    static <T> JavaStream<T> of(T... elements) {
        return StreamableManager.from(Arrays.stream(elements).iterator())
                .as(JavaStream());
    }

    /**
     * Returns an infinite {@code JavaStream} produced by iterative
     * application of a function {@code f} to an initial element {@code seed},
     * producing a {@code JavaStream} consisting of {@code seed}, {@code f(seed)},
     * {@code f(f(seed))}, etc.
     *
     * <p>The first element (position {@code 0}) in the {@code JavaStream} will be
     * the provided {@code seed}.  For {@code n > 0}, the element at position
     * {@code n}, will be the result of applying the function {@code f} to the
     * element at position {@code n - 1}.
     *
     * <p>The action of applying {@code f} for one element
     * <a href="../concurrent/package-summary.html#MemoryVisibility"><i>happens-before</i></a>
     * the action of applying {@code f} for subsequent elements.  For any given
     * element the action may be performed in whatever thread the library
     * chooses.
     *
     * @param <T>  the type of stream elements
     * @param seed the initial element
     * @param f    a function to be applied to the previous element to produce
     *             a new element
     * @return a new {@code JavaStream}
     */
    static <T> JavaStream<T> iterate(final T seed, final UnaryOperator<T> f) {
        return iterate(seed, t -> true, f);
    }

    /**
     * Returns a {@code JavaStream} produced by iterative
     * application of the given {@code next} function to an initial element,
     * conditioned on satisfying the given {@code hasNext} predicate.  The
     * stream terminates as soon as the {@code hasNext} predicate returns false.
     *
     * <p>{@code Stream.iterate} should produce the same sequence of elements as
     * produced by the corresponding for-loop:
     * <pre>{@code
     *     for (T index=seed; hasNext.test(index); index = next.apply(index)) {
     *         ...
     *     }
     * }</pre>
     *
     * <p>The resulting sequence may be empty if the {@code hasNext} predicate
     * does not hold on the seed value. Otherwise the first element will be the
     * supplied {@code seed} value, the next element (if present) will be the
     * result of applying the {@code next} function to the {@code seed} value,
     * and so on iteratively until the {@code hasNext} predicate indicates that
     * the stream should terminate.
     *
     * <p>The action of applying the {@code hasNext} predicate to an element
     * <a href="../concurrent/package-summary.html#MemoryVisibility"><i>happens-before</i></a>
     * the action of applying the {@code next} function to that element.  The
     * action of applying the {@code next} function for one element
     * <i>happens-before</i> the action of applying the {@code hasNext}
     * predicate for subsequent elements.  For any given element an action may
     * be performed in whatever thread the library chooses.
     *
     * @param <T>     the type of stream elements
     * @param seed    the initial element
     * @param hasNext a predicate to apply to elements to determine when the
     *                stream must terminate.
     * @param next    a function to be applied to the previous element to produce
     *                a new element
     * @return a new {@code JavaStream}
     */
    static <T> JavaStream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        return StreamableManager.from(new Iterator<T>() {
            private T current = seed;

            @Override
            public boolean hasNext() {
                return hasNext.test(current);
            }

            @Override
            public T next() {
                T previous = current;
                current = next.apply(current);
                return previous;
            }
        }).as(JavaStream());
    }

    /**
     * Returns an infinite stream where each element is
     * generated by the provided {@code Supplier}. This is suitable for
     * generating constant streams, streams of random elements, etc.
     *
     * @param <T> the type of stream elements
     * @param s   the {@code Supplier} of generated elements
     * @return a new infinite {@code JavaStream}
     */
    static <T> JavaStream<T> generate(Supplier<? extends T> s) {
        return iterate(s.get(), t -> true, t -> s.get());
    }

    /**
     * Returns a stream where each element is provided by
     * the provided {@code Stream}. This is suitable for
     * tapping into a more powerful stream API.
     *
     * @param <T>    the type of stream elements
     * @param stream the {@code Stream} to wrap
     * @return a new {@code JavaStream}
     */
    static <T> JavaStream<T> from(Stream<T> stream) {
        return StreamableManager.from(stream.iterator())
                .as(JavaStream());
    }

    /**
     * Returns a stream where each element is provided by
     * the provided {@code Iterable}. This can be used for
     * tapping into a more powerful stream API.
     *
     * @param <T>      the type of stream elements
     * @param iterable the {@code Iterable} to wrap
     * @return a new {@code JavaStream}
     * @implNote 1. supplying a {@code JavaStream} will not create a new {@code JavaStream}<br>
     * 2. supplying a {@code Streamable} will call {@link #as(Class)} on it to convert it to a {@code JavaStream}.
     */
    static <T> JavaStream<T> from(Iterable<T> iterable) {
        return switch (iterable) {
            case JavaStream<T> javaStream -> javaStream;
            case Streamable<?, T> streamable -> streamable.as(JavaStream());
            default -> StreamableManager.from(iterable.iterator())
                    .as(JavaStream());
        };
    }

    /**
     * Returns a stream where each element is provided by
     * the provided {@code Iterator}. This is suitable for
     * tapping into a more powerful stream API.
     *
     * @param <T>      the type of stream elements
     * @param iterator the {@code Iterator} to wrap
     * @return a new {@code JavaStream}
     */
    static <T> JavaStream<T> from(Iterator<T> iterator) {
        return StreamableManager.from(iterator)
                .as(JavaStream());
    }

    /**
     * Change the stream to the new type.
     *
     * @param clazz the new type for the stream
     * @param <N>   the new stream type
     * @return the new stream
     */
    <N extends Streamable<N, ? super T>> N as(Class<N> clazz);

    /**
     * Sets the {@param maxParallelism} to 1 number. The next stream step will be executed sequentially.
     * <p>This is an intermediate operation.</p>
     *
     * @return a sequential stream
     */
    default S sequential() {
        ((InternalStreamable) this).setMaxParallelTasks(1);
        return (S) this;
    }

    /**
     * Sets the {@param maxParallelism} to the specified number. The next stream step will be executed with
     * the supplied {@param maxParallelism}.
     * <p>This is an intermediate operation.</p>
     *
     * @param maxParallelism the maximum parallelism that is allowed to happen
     * @return a parallel stream
     */
    default S parallel(int maxParallelism) {
        if (maxParallelism < 2) {
            throw new IllegalArgumentException("maxParallelism must be greater than or equal to 2");
        }
        ((InternalStreamable) this).setMaxParallelTasks(maxParallelism);
        return (S) this;
    }

    /**
     * Returns whether the last stream step would execute in parallel.
     *
     * @return {@code true} if the last stream step would execute in parallel if executed
     */
    default boolean isParallel() {
        return ((InternalStreamable) this).getMaxParallelTasks() > 0;
    }

    <R, C, N extends Streamable<N, R>> N gather(StreamableGatherer<? super T, C, R> gatherer);

    <R, C, N extends Streamable<N, R>> N flatGather(StreamableGatherer<? super T, C, Iterable<R>> gatherer);

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable
     * reduction</a> operation on the elements of this stream using a
     * {@code StreamableCollector}.  A {@code StreamableCollector}
     * encapsulates the functions used as arguments to
     * {@link #collect(Supplier, BiConsumer, BiConsumer)}, allowing for reuse of
     * collection strategies and composition of collect operations such as
     * multiple-level grouping or partitioning.
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
     * @param <R> the type of the result
     * @param <C> the intermediate accumulation type of the {@code StreamableCollector}
     * @param collector the {@code StreamableCollector} describing the reduction
     * @return the result of the reduction
     */
    <R, C> R collect(StreamableCollector<? super T, C, R> collector);

    /**
     * Returns an equivalent stream with an additional close handler. Close
     * handlers are run when the {@link #close()} method
     * is called on the stream, and are executed in the order they were
     * added. If any close handler throws an exception, the first
     * exception thrown will be relayed to the caller of {@code close()}.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param closeHandler A task to execute when the stream is closed
     * @return a stream with a handler that is run if the stream is closed
     */
    default S onClose(Runnable closeHandler) {
        return (S)(((InternalStreamable) this).setNext(new OnCloseStep(closeHandler)));
    }

    /**
     * Closes this stream, causing all close handlers for this stream pipeline
     * to be called.
     *
     * @see AutoCloseable#close()
     */
    // TODO: Better implement both close as well as onClose in conjunction with .zip from ZipStream
    void close();

    /**
     * {@inheritDoc}
     */
    @Override
    default void forEach(Consumer<? super T> action) {
        collect(new StreamableCollector.Simple<T, T>() {
            @Override
            public Ordering ordering() {
                return Ordering.ORDERED;
            }

            @Override
            public boolean accumulate(long index, T element) {
                action.accept(element);
                return false;
            }

            @Override
            public T finish() {
                return null;
            }
        });
    }
}
