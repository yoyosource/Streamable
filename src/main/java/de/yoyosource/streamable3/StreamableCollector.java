package de.yoyosource.streamable3;

public interface StreamableCollector<T, A, R> {
    A container();
    boolean accumulate(A container, long index, T element);
    A combine(A firstContainer, A secondContainer);
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

        public abstract boolean accumulate(long index, T element);

        @Override
        public final Object combine(Object firstContainer, Object secondContainer) {
            return null;
        }

        @Override
        public final R finish(Object container) {
            return finish();
        }

        public abstract R finish();
    }

    default void onClose() {
    }
}
