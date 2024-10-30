package de.yoyosource.streamable.impl;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.StreamableGatherer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface ComparableStream<T extends Comparable<T>> extends Streamable<T> {

    static <T extends Comparable<T>> Class<ComparableStream<T>> type() {
        return (Class<ComparableStream<T>>) (Class) ComparableStream.class;
    }

    default ComparableStream<T> sorted() {
        return flatGather(new StreamableGatherer<>() {
            private List<T> elements = new ArrayList<>();

            @Override
            public boolean apply(T input, Consumer<Iterable<T>> next) {
                elements.add(input);
                return false;
            }

            @Override
            public void finish(Consumer<Iterable<T>> next) {
                elements.sort(null);
                next.accept(elements);
            }
        });
    }

    default Optional<T> min() {
        return collect(new StreamableCollector<>() {
            private T current = null;

            @Override
            public boolean apply(T input) {
                if (current == null) {
                    current = input;
                    return false;
                }
                if (input == null) {
                    return false;
                }
                if (current.compareTo(input) > 0) {
                    current = input;
                }
                return false;
            }

            @Override
            public Optional<T> finish() {
                return Optional.ofNullable(current);
            }
        });
    }

    default Optional<T> max() {
        return collect(new StreamableCollector<>() {
            private T current = null;

            @Override
            public boolean apply(T input) {
                if (current == null) {
                    current = input;
                    return false;
                }
                if (input == null) {
                    return false;
                }
                if (current.compareTo(input) < 0) {
                    current = input;
                }
                return false;
            }

            @Override
            public Optional<T> finish() {
                return Optional.ofNullable(current);
            }
        });
    }
}
