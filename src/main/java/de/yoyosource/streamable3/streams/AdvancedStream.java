package de.yoyosource.streamable3.streams;

import de.yoyosource.streamable3.Streamable;

public interface AdvancedStream<T> extends Streamable<AdvancedStream<T>, T> {

    static <T> Class<AdvancedStream<T>> AdvancedStream() {
        return (Class<AdvancedStream<T>>) (Class) AdvancedStream.class;
    }

    static <T> Class<AdvancedStream<T>> AdvancedStream(Class<T> clazz) {
        return (Class<AdvancedStream<T>>) (Class) AdvancedStream.class;
    }
}
