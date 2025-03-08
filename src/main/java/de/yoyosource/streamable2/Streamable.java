package de.yoyosource.streamable2;

import java.util.function.Consumer;

public interface Streamable<T> extends Iterable<T> {

    static <T> Class<Streamable<T>> type() {
        return (Class<Streamable<T>>) (Class) Streamable.class;
    }

    @Internal
    <R, S extends Streamable<R>, A> S gather(StreamableGatherer<? super T, A, R> gatherer);

    @Internal
    <R, S extends Streamable<R>, A> S flatGather(StreamableGatherer<? super T, A, Iterable<R>> gatherer);

    @Internal
    <R, A> R collect(StreamableCollector<? super T, A, R> collector);

    @Internal
    <S extends Streamable<T>> S as(Class<S> clazz);

    @Internal
    <S extends Streamable<T>> S parallel();

    @Internal
    <S extends Streamable<T>> S sequential();

    @Override
    default void forEach(Consumer<? super T> action) {
        collect(new StreamableCollector.Simple<>() {
            @Override
            public boolean accumulate(T element, long index) {
                action.accept(element);
                return false;
            }

            @Override
            public Object finish() {
                return null;
            }
        });
    }
}
