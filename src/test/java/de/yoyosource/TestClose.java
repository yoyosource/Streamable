package de.yoyosource;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import org.junit.jupiter.api.Test;

class TestClose {

    @Test
    void test() {
        Streamable.of(1, 2, 3)
                .onClose(() -> {
                    System.out.println("Close called");
                })
                .as(AdvancedStream.AdvancedStream())
                .concat(Streamable.of(4, 5, 6).onClose(() -> {
                    System.out.println("Close called");
                }))
                .iterator()
                .forEachRemaining(System.out::println);
    }
}
