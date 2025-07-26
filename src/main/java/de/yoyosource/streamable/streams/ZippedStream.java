package de.yoyosource.streamable.streams;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableGatherer;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ZippedStream<A, B> extends Streamable<ZippedStream<A, B>, ZippedStream.Zip<A, B>> {

    /**
     * Returns a {@code Class} instance with the generic type of {@code ZippedStream} for {@link #as(Class)} method.
     *
     * @param <A> the first type of elements inside the {@code ZippedStream}
     * @param <B> the second type of elements inside the {@code ZippedStream}
     * @return the type for {@link #as(Class)}
     */
    static <A, B> Class<ZippedStream<A, B>> ZippedStream() {
        return (Class<ZippedStream<A, B>>) (Class) ZippedStream.class;
    }

    /**
     * Returns a {@code Class} instance with the generic type of {@code ZippedStream} for {@link #as(Class)} method.
     *
     * @param <A> the first type of elements inside the {@code ZippedStream}
     * @param <B> the second type of elements inside the {@code ZippedStream}
     * @param first the type {@code A} should be
     * @param second the type {@code B} should be
     * @return the type for {@link #as(Class)}
     */
    static <A, B> Class<ZippedStream<A, B>> ZippedStream(Class<A> first, Class<B> second) {
        return (Class<ZippedStream<A, B>>) (Class) ZippedStream.class;
    }

    final class Zip<A, B> {
        public final A a;
        public final B b;

        public Zip(A a, B b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public String toString() {
            return "[" + a + ", " + b + "]";
        }
    }

    default <C> JavaStream<C> map(BiFunction<A, B, C> mapper) {
        return gather(new StreamableGatherer.Simple<Zip<A, B>, C>() {
            @Override
            public boolean integrate(long index, Zip<A, B> input, Consumer<? super C> next) {
                next.accept(mapper.apply(input.a, input.b));
                return false;
            }

            @Override
            public void finish(Consumer<? super C> next) {
            }
        }).as(JavaStream.JavaStream());
    }

    default ZippedStream<A, B> filter(BiPredicate<A, B> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, Zip<A, B> input, Consumer<? super Zip<A, B>> next) {
                if (predicate.test(input.a, input.b)) {
                    next.accept(input);
                }
                return false;
            }

            @Override
            public void finish(Consumer<? super Zip<A, B>> next) {
            }
        });
    }

    default ZippedStream<A, B> filterLeft(Predicate<A> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, Zip<A, B> input, Consumer<? super Zip<A, B>> next) {
                if (predicate.test(input.a)) {
                    next.accept(input);
                }
                return false;
            }

            @Override
            public void finish(Consumer<? super Zip<A, B>> next) {
            }
        });
    }

    default ZippedStream<A, B> filterRight(Predicate<B> predicate) {
        return gather(new StreamableGatherer.Simple<>() {
            @Override
            public boolean integrate(long index, Zip<A, B> input, Consumer<? super Zip<A, B>> next) {
                if (predicate.test(input.b)) {
                    next.accept(input);
                }
                return false;
            }

            @Override
            public void finish(Consumer<? super Zip<A, B>> next) {
            }
        });
    }

    default JavaStream<A> getLeft() {
        return gather(new StreamableGatherer.Simple<Zip<A, B>, A>() {
            @Override
            public boolean integrate(long index, Zip<A, B> input, Consumer<? super A> next) {
                next.accept(input.a);
                return false;
            }

            @Override
            public void finish(Consumer<? super A> next) {
            }
        }).as(JavaStream.JavaStream());
    }

    default JavaStream<B> getRight() {
        return gather(new StreamableGatherer.Simple<Zip<A, B>, B>() {
            @Override
            public boolean integrate(long index, Zip<A, B> input, Consumer<? super B> next) {
                next.accept(input.b);
                return false;
            }

            @Override
            public void finish(Consumer<? super B> next) {
            }
        }).as(JavaStream.JavaStream());
    }
}
