package de.yoyosource.streamable.internal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public interface Element<T> {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class Finish<T> implements Element<T> {

        private static final Finish INSTANCE = new Finish();

        public static <T> Finish<T> getInstance() {
            return INSTANCE;
        }

        @Override
        public String toString() {
            return "Element{EOS}";
        }
    }

    record Value<T>(long index, T value) implements Element<T> {

        @Override
        public String toString() {
            return "Element{" +
                    "index=" + index +
                    ", value=" + value +
                    '}';
        }
    }
}
