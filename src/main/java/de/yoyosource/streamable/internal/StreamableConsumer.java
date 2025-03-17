package de.yoyosource.streamable.internal;

public interface StreamableConsumer {
    default void consume(Element element) {
        throw new UnsupportedOperationException();
    }

    default void consume(long index, Object value) {
        consume(new Element.Value(index, value));
    }
    default void finish() {
        consume(new Element.Finish());
    }
}
