package de.yoyosource.streamable2;

public interface StreamableCollector<T, A, R> {
    A container();
    boolean accumulate(A container, T element, long index);
    A combine(A firstContainer, A secondContainer);
    R finish(A container);

    abstract class Simple<T, A, R> implements StreamableCollector<T, A, R> {
        @Override
        public final A container() {
            return null;
        }

        @Override
        public final boolean accumulate(A container, T element, long index) {
            return accumulate(element, index);
        }

        public abstract boolean accumulate(T element, long index);

        @Override
        public final A combine(A firstContainer, A secondContainer) {
            return null;
        }

        @Override
        public final R finish(A container) {
            return finish();
        }

        public abstract R finish();
    }

    default void onClose() {
    }
}
