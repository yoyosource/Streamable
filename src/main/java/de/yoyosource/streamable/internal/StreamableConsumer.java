package de.yoyosource.streamable.internal;

import de.yoyosource.streamable.Evaluation;
import de.yoyosource.streamable.Ordering;

import java.util.Set;

public interface StreamableConsumer {
    Ordering ordering();
    Set<Evaluation> evaluation();

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
