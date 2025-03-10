package de.yoyosource.streamable3;

import java.util.function.Consumer;

public interface Streamable<S extends Streamable<S, T>, T> extends Iterable<T> {

    static <S extends Streamable<S, T>, T> Class<Streamable<S, T>> type() {
        return (Class<Streamable<S, T>>) (Class) Streamable.class;
    }

    <S extends Streamable<S, T>> S as(Class<S> clazz);

    S sequential();
    S parallel(int maxParallelism);

    <R, A, S extends Streamable<S, R>> S gather(StreamableGatherer<? super T, A, R> gatherer);
    // <R, A, S extends Streamable<S, R>> S flatGather(StreamableGatherer<? super T, A, Iterable<R>> gatherer);
    <R, A> R collect(StreamableCollector<? super T, A, R> collector);

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
