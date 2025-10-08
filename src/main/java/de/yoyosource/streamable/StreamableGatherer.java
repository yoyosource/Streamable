package de.yoyosource.streamable;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public interface StreamableGatherer<T, A, R> {

    /**
     * The {@link Ordering} this {@link StreamableGatherer}
     * needs to work properly.
     *
     * @implSpec The default implementation will return {@link Ordering#UNORDERED}.
     *
     * @return The needed Ordering
     */
    default Ordering ordering() {
        return Ordering.UNORDERED;
    }

    /**
     * The Evaluation and optimizations that should be used
     * for the this {@link StreamableCollector}.
     *
     * @implSpec The default implementation will return {@link Collections#emptySet()}.
     *
     * @return The desired optimizations.
     */
    default Set<Evaluation> evaluation() {
        return Collections.emptySet();
    }

    /**
     * Produces an instance of the intermediate state used for this
     * gathering operation.
     *
     * @return An instance of the intermediate state
     * used for this gathering operation
     */
    A container();

    /**
     * Performs an action given: the current state, the next element, and
     * a downstream object; potentially inspecting and/or updating
     * the state, optionally sending any number of elements downstream
     * -- and then returns whether more elements are to be consumed or not.
     *
     * @param container The state to integrate into
     * @param index The index of the current Element
     * @param element The element to integrate
     * @param next The downstream object of this integration
     * @return {@code false} if subsequent integration is desired,
     *         {@code true} if not
     */
    boolean integrate(A container, long index, T element, Consumer<? super R> next);

    /**
     * Accepts two intermediate states and combines them into one.
     *
     * @param firstContainer The first state to combine
     * @param secondContainer The second state to combine
     * @return Accepts two intermediate states and combines
     *         them into one
     */
    A combine(A firstContainer, A secondContainer);

    /**
     * Accepts the final intermediate state and a {@link Consumer} object,
     * allowing to perform a final action at the end of input elements.
     *
     * @param container The state to finish
     * @param next The downstream object of this integration
     */
    void finish(A container, Consumer<? super R> next);

    /**
     * A Stateless {@link StreamableGatherer} for simpler
     * {@link Streamable#gather(StreamableGatherer)} or
     * {@link Streamable#flatGather(StreamableGatherer)}
     * operations.
     *
     * @param <T> The input type
     * @param <R> The result type
     */
    abstract class Simple<T, R> implements StreamableGatherer<T, Object, R> {
        @Override
        public Set<Evaluation> evaluation() {
            return Evaluation.noContainer;
        }

        @Override
        public final Object container() {
            return null;
        }

        @Override
        public final boolean integrate(Object container, long index, T element, Consumer<? super R> next) {
            return integrate(index, element, next);
        }

        /**
         * Performs an action given: the next element, and
         * a downstream object; optionally sending any number
         * of elements downstream -- and then returns whether
         * more elements are to be consumed or not.
         *
         * @param index The index of the current Element
         * @param element The element to integrate
         * @param next The downstream object of this integration
         * @return {@code true} if subsequent integration is desired,
         *         {@code false} if not
         */
        public abstract boolean integrate(long index, T element, Consumer<? super R> next);

        @Override
        public final Object combine(Object firstContainer, Object secondContainer) {
            return null;
        }

        @Override
        public final void finish(Object container, Consumer<? super R> next) {
            finish(next);
        }

        /**
         * Allows to perform a final action at the end of input elements.
         *
         * @param next The downstream object of this integration
         */
        public abstract void finish(Consumer<? super R> next);
    }
}
