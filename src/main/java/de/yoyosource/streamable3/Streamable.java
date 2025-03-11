package de.yoyosource.streamable3;

import de.yoyosource.streamable3.impl.JavaStream;

import java.util.Collections;
import java.util.function.Consumer;

import static de.yoyosource.streamable3.impl.JavaStream.JavaStream;

public interface Streamable<S extends Streamable<S, T>, T> extends Iterable<T> {

    static <T> JavaStream<T> of(T element) {
        return StreamableManager.from(Collections.singletonList(element).iterator())
                .as(JavaStream());
    }

    <N extends Streamable<N, T>> N as(Class<N> clazz);

    S sequential();
    S parallel(int maxParallelism);

    <R, C, N extends Streamable<N, R>> N gather(StreamableGatherer<? super T, C, R> gatherer);
    <R, C, N extends Streamable<N, R>> N flatGather(StreamableGatherer<? super T, C, Iterable<R>> gatherer);
    <R, C> R collect(StreamableCollector<? super T, C, R> collector);

    @Override
    default void forEach(Consumer<? super T> action) {
        collect(new StreamableCollector.Simple<T, T>() {
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
