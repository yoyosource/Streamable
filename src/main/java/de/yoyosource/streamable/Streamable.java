package de.yoyosource.streamable;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Streamable<T> extends Iterable<T> {
    static <T> Streamable<T> from(Stream<T> stream) {
        return StreamableManager.from(stream);
    }

    <R, S extends Streamable<R>> S gather(StreamableGatherer<? super T, R> gatherer);
    <R, S extends Streamable<R>> S flatGather(StreamableGatherer<? super T, Iterable<R>> gatherer);
    <R> R collect(StreamableCollector<? super T, R> collector);

    <S extends Streamable<T>> S as(Class<S> clazz);

    @Override
    default void forEach(Consumer<? super T> action) {
        collect(new StreamableCollector<>() {
            @Override
            public boolean apply(T input) {
                action.accept(input);
                return false;
            }

            @Override
            public Object finish() {
                return null;
            }
        });
    }
}
