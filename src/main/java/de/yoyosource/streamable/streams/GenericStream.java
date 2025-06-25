package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;

import java.util.function.Consumer;
import java.util.function.Function;

public interface GenericStream<S extends Streamable<S, T>, T> extends Streamable<GenericStream<S, T>, S> {

    static <S extends Streamable<S, T>, T> Class<GenericStream<S, T>> GenericStream() {
        return (Class<GenericStream<S, T>>) (Class) GenericStream.class;
    }

    static <S extends Streamable<S, T>, T> Class<GenericStream<S, T>> GenericStream(Class<S> clazz1, Class<T> clazz2) {
        return (Class<GenericStream<S, T>>) (Class) GenericStream.class;
    }

    default <R, N extends Streamable<N, R>> GenericStream<N, R> gatherEach(Function<S, N> mapper) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, S element, Consumer<? super N> next) {
                next.accept(mapper.apply(element));
                return false;
            }

            @Override
            public void finish(Consumer<? super N> next) {
            }
        });
    }

    default <R> JavaStream<R> collectEach(Function<S, R> mapper) {
        return gather(new StreamableGatherer.Simple<S, R>() {
            @Override
            public boolean integrate(long index, S element, Consumer<? super R> next) {
                next.accept(mapper.apply(element));
                return false;
            }

            @Override
            public void finish(Consumer<? super R> next) {
            }
        }).as(JavaStream.JavaStream());
    }

    default JavaStream<T> flatten() {
        return flatGather(new StreamableGatherer.Simple<Iterable<T>, Iterable<T>>() {
            @Override
            public boolean integrate(long index, Iterable<T> element, Consumer<? super Iterable<T>> next) {
                next.accept(element);
                return false;
            }

            @Override
            public void finish(Consumer<? super Iterable<T>> next) {
            }
        }).as(JavaStream.JavaStream());
    }
}
