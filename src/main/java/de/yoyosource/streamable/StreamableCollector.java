package de.yoyosource.streamable;

public interface StreamableCollector<I, R> {
    void apply(I input); // TODO: This could maybe return a boolean to simplify some .gather().collect() statements.
    R finish();
}
