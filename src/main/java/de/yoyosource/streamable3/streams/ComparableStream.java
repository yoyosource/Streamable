package de.yoyosource.streamable3.streams;

import de.yoyosource.streamable3.data.SingleData;
import de.yoyosource.streamable3.Streamable;
import de.yoyosource.streamable3.StreamableCollector;
import de.yoyosource.streamable3.StreamableGatherer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface ComparableStream<T extends Comparable<T>> extends Streamable<ComparableStream<T>, T> {

    static <T extends Comparable<T>> Class<ComparableStream<T>> ComparableStream() {
        return (Class<ComparableStream<T>>) (Class) ComparableStream.class;
    }

    static <T extends Comparable<T>> Class<ComparableStream<T>> ComparableStream(Class<T> clazz) {
        return (Class<ComparableStream<T>>) (Class) ComparableStream.class;
    }

    default ComparableStream<T> sorted() {
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
                next.accept(container);
            }
        });
    }

    default Optional<T> min() {
        return collect(new StreamableCollector<T, SingleData<T>, Optional<T>>() {
            @Override
            public SingleData<T> container() {
                return new SingleData<>(null);
            }

            @Override
            public boolean accumulate(SingleData<T> container, long index, T element) {
                if (container.first == null) {
                    container.first = element;
                    return false;
                }
                if (element == null) {
                    return false;
                }
                if (container.first.compareTo(element) > 0) {
                    container.first = element;
                }
                return false;
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
                return Optional.of(container.first);
            }
        });
    }

    default Optional<T> max() {
        return collect(new StreamableCollector<T, SingleData<T>, Optional<T>>() {
            @Override
            public SingleData<T> container() {
                return new SingleData<>(null);
            }

            @Override
            public boolean accumulate(SingleData<T> container, long index, T element) {
                if (container.first == null) {
                    container.first = element;
                    return false;
                }
                if (element == null) {
                    return false;
                }
                if (container.first.compareTo(element) < 0) {
                    container.first = element;
                }
                return false;
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
                return Optional.of(container.first);
            }
        });
    }
}
