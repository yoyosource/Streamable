package de.yoyosource.streamable3;

import java.util.function.Consumer;

public interface StreamableGatherer<T, A, R> {
    A container();
    boolean integrate(A container, Element.Value<T> element, Consumer<? super R> next);
    A combine(A firstContainer, A secondContainer);
    void finish(A container, Consumer<? super R> next);

    default void onClose() {
    }
}
