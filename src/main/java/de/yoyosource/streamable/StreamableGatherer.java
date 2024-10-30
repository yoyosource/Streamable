package de.yoyosource.streamable;

import java.util.function.Consumer;

public interface StreamableGatherer<I, O> {
    boolean apply(I input, Consumer<O> next);

    void finish(Consumer<O> next);

    default void onClose() {
    }
}
