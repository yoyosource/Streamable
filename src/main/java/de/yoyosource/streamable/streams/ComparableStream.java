package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.Evaluation;
import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.SingleData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface ComparableStream<T extends Comparable<T>> extends Streamable<ComparableStream<T>, T> {

    /**
     * Returns a {@code Class} instance with the generic type of {@code ComparableStream} for {@link #as(Class)} method.
     *
     * @param <T> the type of elements inside the {@code ComparableStream}
     * @return the type for {@link #as(Class)}
     */
    static <T extends Comparable<T>> Class<ComparableStream<T>> ComparableStream() {
        return (Class<ComparableStream<T>>) (Class) ComparableStream.class;
    }

    /**
     * Returns a {@code Class} instance with the generic type of {@code ComparableStream} for {@link #as(Class)} method.
     *
     * @param <T> the type of elements inside the {@code ComparableStream}
     * @param clazz the type {@code T} should be
     * @return the type for {@link #as(Class)}
     */
    static <T extends Comparable<T>> Class<ComparableStream<T>> ComparableStream(Class<T> clazz) {
        return (Class<ComparableStream<T>>) (Class) ComparableStream.class;
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
    default ComparableStream<T> sorted() {
        return flatGather(new StreamableGatherer<T, List<T>, Iterable<T>>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.GREEDY);
            }

            @Override
            public List<T> container() {
                return new ArrayList<>();
            }

            @Override
            public boolean integrate(List<T> container, long index, T element, Consumer<? super Iterable<T>> next) {
                container.add(element);
                return true;
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
        return collect(new StreamableCollector<T, SingleData<T>, Optional<T>>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.GREEDY);
            }

            @Override
            public SingleData<T> container() {
                return new SingleData<>(null);
            }

            @Override
            public boolean accumulate(SingleData<T> container, long index, T element) {
                if (container.first == null) {
                    container.first = element;
                    return true;
                }
                if (element == null) {
                    return true;
                }
                if (container.first.compareTo(element) > 0) {
                    container.first = element;
                }
                return true;
            }

            @Override
            public SingleData<T> combine(SingleData<T> firstContainer, SingleData<T> secondContainer) {
                if (firstContainer.first.compareTo(secondContainer.first) > 0) {
                    firstContainer.first = secondContainer.first;
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
        return collect(new StreamableCollector<T, SingleData<T>, Optional<T>>() {
            @Override
            public Evaluation evaluation() {
                return Evaluation.get(Evaluation.GREEDY);
            }

            @Override
            public SingleData<T> container() {
                return new SingleData<>(null);
            }

            @Override
            public boolean accumulate(SingleData<T> container, long index, T element) {
                if (container.first == null) {
                    container.first = element;
                    return true;
                }
                if (element == null) {
                    return true;
                }
                if (container.first.compareTo(element) < 0) {
                    container.first = element;
                }
                return true;
            }

            @Override
            public SingleData<T> combine(SingleData<T> firstContainer, SingleData<T> secondContainer) {
                if (firstContainer.first.compareTo(secondContainer.first) < 0) {
                    firstContainer.first = secondContainer.first;
                }
                return firstContainer;
            }

            @Override
            public Optional<T> finish(SingleData<T> container) {
                return Optional.ofNullable(container.first);
            }
        });
    }
}
