package de.yoyosource.streamable;

public interface StreamableCollector<I, R> {
    boolean apply(I input);
    R finish();

    default void onClose() {
    }
}
