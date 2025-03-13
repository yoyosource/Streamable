package de.yoyosource.streamable3;

import de.yoyosource.streamable3.data.SingleData;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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

    // Special internal API methods or classes

    default StreamableGatherer<T, A, R> toGatherer() {
        StreamableCollector<T, A, R> collector = this;
        return new StreamableGatherer<>() {
            @Override
            public A container() {
                return collector.container();
            }

            @Override
            public boolean integrate(A container, long index, T element, Consumer<? super R> next) {
                return collector.accumulate(container, index, element);
            }

            @Override
            public A combine(A firstContainer, A secondContainer) {
                return collector.combine(firstContainer, secondContainer);
            }

            @Override
            public void finish(A container, Consumer<? super R> next) {
                next.accept(collector.finish(container));
            }
        };
    }

    class Last<R> implements StreamableCollector<R, SingleData<R>, R> {
        @Override
        public SingleData<R> container() {
            return new SingleData<>(null);
        }

        @Override
        public boolean accumulate(SingleData<R> container, long index, R element) {
            container.first = element;
            return false;
        }

        @Override
        public SingleData<R> combine(SingleData<R> firstContainer, SingleData<R> secondContainer) {
            return null;
        }

        @Override
        public R finish(SingleData<R> container) {
            return container.first;
        }
    }

    class First<R> implements StreamableCollector<R, SingleData<R>, R> {
        @Override
        public SingleData<R> container() {
            return new SingleData<>(null);
        }

        @Override
        public boolean accumulate(SingleData<R> container, long index, R element) {
            container.first = element;
            return true;
        }

        @Override
        public SingleData<R> combine(SingleData<R> firstContainer, SingleData<R> secondContainer) {
            return null;
        }

        @Override
        public R finish(SingleData<R> container) {
            return container.first;
        }
    }
}
