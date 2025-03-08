package de.yoyosource.streamable2;

import java.util.function.Consumer;

public interface StreamableGatherer<T, A, R> {
    A container();
    boolean integrate(A container, T element, long index, Consumer<? super R> next);
    A combine(A firstContainer, A secondContainer);
    void finish(A container, Consumer<? super R> next);

    abstract class Simple<T, A, R> implements StreamableGatherer<T, A, R> {
        @Override
        public final A container() {
            return null;
        }

        @Override
        public final boolean integrate(A container, T element, long index, Consumer<? super R> next) {
            return integrate(element, index, next);
        }

        public abstract boolean integrate(T element, long index, Consumer<? super R> next);

        @Override
        public final A combine(A firstContainer, A secondContainer) {
            return null;
        }

        @Override
        public final void finish(A container, Consumer<? super R> next) {
            finish(next);
        }

        public abstract void finish(Consumer<? super R> next);
    }

    default void onClose() {
    }
}
