package de.yoyosource.streamable.internal;

import de.yoyosource.streamable.Evaluation;

public interface StreamableConsumer {
    Evaluation evaluation();

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
