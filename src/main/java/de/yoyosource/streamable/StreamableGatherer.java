package de.yoyosource.streamable;

import java.util.function.Consumer;

import static de.yoyosource.streamable.Evaluation.NO_CONTAINER;

public interface StreamableGatherer<T, A, R> {

    static Evaluation getEvaluation(StreamableGatherer<?, ?, ?> gatherer) {
        Evaluation evaluation = gatherer.evaluation();
        if (gatherer instanceof StreamableGatherer.Simple<?, ?> && !evaluation.contains(NO_CONTAINER)) {
            return evaluation.with(NO_CONTAINER);
        } else if (!(gatherer instanceof StreamableGatherer.Simple<?, ?>) && evaluation.contains(NO_CONTAINER)) {
            return evaluation.without(NO_CONTAINER);
        }
        return evaluation;
    }

    /**
     * The Evaluation and optimizations that should be used
     * for the this {@link StreamableCollector}. This also
     * includes the ordering required for this element.
     * Both {@link Evaluation#UNORDERED} and {@link Evaluation#ORDERED}
     * will have an effect on the Streamable processing before
     * this element. The {@link Evaluation#GREEDY} will
     * also have an effect.
     *
     * @implSpec The default implementation will return {@link Evaluation#UNORDERED}.
     *
     * @return The desired optimizations.
     */
    default Evaluation evaluation() {
        return Evaluation.UNORDERED;
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
     * @return {@code true} if subsequent integration is desired,
     *         {@code false} if not
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
        public Evaluation evaluation() {
            return Evaluation.get(Evaluation.UNORDERED, NO_CONTAINER);
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
