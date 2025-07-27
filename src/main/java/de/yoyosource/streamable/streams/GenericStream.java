package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.InternalStreamable;
import de.yoyosource.streamable.internal.step.FlattenStep;

import java.util.function.Consumer;
import java.util.function.Function;

public interface GenericStream<S extends Streamable<S, T>, T> extends Streamable<GenericStream<S, T>, S> {

    /**
     * Returns a {@code Class} instance with the generic type of {@code GenericStream} for {@link #as(Class)} method.
     *
     * @param <S> the {@code Streamable} type to be
     * @param <T> the type of the elements in the {@code Streamable}
     * @return the type for {@link #as(Class)}
     */
    static <S extends Streamable<S, T>, T> Class<GenericStream<S, T>> GenericStream() {
        return (Class<GenericStream<S, T>>) (Class) GenericStream.class;
    }

    /**
     * Returns a {@code Class} instance with the generic type of {@code GenericStream} for {@link #as(Class)} method.
     *
     * @param <S> the {@code Streamable} type to be
     * @param <T> the type of the elements in the {@code Streamable}
     * @param streamable the type {@code S} should be
     * @param element the type {@code T} should be
     * @return the type for {@link #as(Class)}
     */
    static <S extends Streamable<S, T>, T> Class<GenericStream<S, T>> GenericStream(Class<S> streamable, Class<T> element) {
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
        return (JavaStream<T>) ((GenericStream)((InternalStreamable) this).setNext(new FlattenStep())).as(JavaStream.JavaStream());
    }
}
