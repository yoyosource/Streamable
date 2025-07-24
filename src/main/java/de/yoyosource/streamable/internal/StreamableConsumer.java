package de.yoyosource.streamable.internal;

import de.yoyosource.streamable.Ordering;

public interface StreamableConsumer {
    Ordering ordering();

    default void consume(Element element) {
        throw new UnsupportedOperationException();
    }

    default void consume(long index, Object value) {
        consume(new Element.Value(index, value));
    }
    default void finish() {
        consume(Element.Finish.getInstance());
    }
}
