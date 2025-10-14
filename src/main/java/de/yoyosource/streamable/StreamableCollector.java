package de.yoyosource.streamable;

public interface StreamableCollector<T, A, R> {

    static Evaluation2 getEvaluation(StreamableCollector<?, ?, ?> collector) {
        Evaluation2 evaluation = collector.evaluation();
        if (collector instanceof StreamableCollector.Simple<?, ?> && !evaluation.contains(Evaluation2.NO_CONTAINER)) {
            return evaluation.with(Evaluation2.NO_CONTAINER);
        } else if (!(collector instanceof StreamableCollector.Simple<?, ?>) && evaluation.contains(Evaluation2.NO_CONTAINER)) {
            return evaluation.without(Evaluation2.NO_CONTAINER);
        }
        return evaluation;
    }

    /**
     * The Evaluation and optimizations that should be used
     * for the this {@link StreamableCollector}. This also
     * includes the ordering required for this element.
     * Both {@link Evaluation2#UNORDERED} and {@link Evaluation2#ORDERED}
     * will have an effect on the Streamable processing before
     * this element. The {@link Evaluation2#GREEDY} will
     * also have an effect.
     *
     * @implSpec The default implementation will return {@link Evaluation2#UNORDERED}.
     *
     * @return The desired optimizations.
     */
    default Evaluation2 evaluation() {
        return Evaluation2.UNORDERED;
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
     * Performs an action given: the current state, the next element;
     * potentially inspecting and/or updating the state -- and then
     * returns whether more elements are to be consumed or not.
     *
     * @param container The state to integrate into
     * @param index The index of the current Element
     * @param element The element to integrate
     * @return {@code true} if subsequent integration is desired,
     *         {@code false} if not
     */
    boolean accumulate(A container, long index, T element);

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
     * Accepts the final intermediate state allowing to perform a final
     * action at the end of input elements.
     *
     * @param container The state to finish
     * @return The result of the {@link Streamable} call
     */
    R finish(A container);

    abstract class Simple<T, R> implements StreamableCollector<T, Object, R> {
        @Override
        public final Object container() {
            return null;
        }

        @Override
        public final boolean accumulate(Object container, long index, T element) {
            return accumulate(index, element);
        }

        /**
         * Performs an action given: the next element;
         * -- and then returns whether more elements are
         * to be consumed or not.
         *
         * @param index The index of the current Element
         * @param element The element to integrate
         * @return {@code true} if subsequent integration is desired,
         *         {@code false} if not
         */
        public abstract boolean accumulate(long index, T element);

        @Override
        public final Object combine(Object firstContainer, Object secondContainer) {
            return null;
        }

        @Override
        public final R finish(Object container) {
            return finish();
        }

        /**
         * Allows to perform a final action at the end of input elements.
         *
         * @return The result of the {@link Streamable} call
         */
        public abstract R finish();
    }
}
