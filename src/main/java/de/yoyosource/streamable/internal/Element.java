package de.yoyosource.streamable.internal;

public interface Element<T> {

    record Finish<T>() implements Element<T> {

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
