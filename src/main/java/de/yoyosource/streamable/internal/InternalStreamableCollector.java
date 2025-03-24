package de.yoyosource.streamable.internal;

import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.data.SingleData;

public class InternalStreamableCollector {

    public static class First<R> implements StreamableCollector<R, SingleData<R>, R> {
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

    public static class Last<R> implements StreamableCollector<R, SingleData<R>, R> {
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
}
