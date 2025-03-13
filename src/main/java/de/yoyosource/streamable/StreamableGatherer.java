package de.yoyosource.streamable;

import java.util.function.Consumer;

public interface StreamableGatherer<T, A, R> {
    A container();
    boolean integrate(A container, long index, T element, Consumer<? super R> next);
    A combine(A firstContainer, A secondContainer);
    void finish(A container, Consumer<? super R> next);

    abstract class Simple<T, R> implements StreamableGatherer<T, Object, R> {
        @Override
        public final Object container() {
            return null;
        }

        @Override
        public final boolean integrate(Object container, long index, T element, Consumer<? super R> next) {
            return integrate(index, element, next);
        }

        public abstract boolean integrate(long index, T element, Consumer<? super R> next);

        @Override
        public final Object combine(Object firstContainer, Object secondContainer) {
            return null;
        }

        @Override
        public final void finish(Object container, Consumer<? super R> next) {
            finish(next);
        }

        public abstract void finish(Consumer<? super R> next);
    }

    default void onClose() {
    }
}
