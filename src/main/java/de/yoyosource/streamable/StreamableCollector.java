package de.yoyosource.streamable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface StreamableCollector<T, A, R> {

    static Set<Evaluation> getEvaluation(StreamableCollector<?, ?, ?> collector) {
        Set<Evaluation> evaluation = collector.evaluation();
        if (collector instanceof StreamableCollector.Simple<?,?> && !evaluation.contains(Evaluation.NO_CONTAINER)) {
            evaluation = new HashSet<>(evaluation);
            evaluation.add(Evaluation.NO_CONTAINER);
        }
        return evaluation;
    }

    /**
     * The {@link Ordering} this {@link StreamableCollector}
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
