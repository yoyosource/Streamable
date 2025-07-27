package de.yoyosource.streamable;

import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class CloseTest {

    @Test
    void testClose() {
        AtomicBoolean closed = new AtomicBoolean(false);
        Streamable.of(1, 2, 3)
                .onClose(() -> closed.set(true))
                .forEach((i) -> {
                    if (closed.get()) Assertions.fail("Streamable.close() should not be called before last Element");
                });
        Assertions.assertTrue(closed.get());
    }

    @Test
    void testManuallyClose() {
        AtomicBoolean closed = new AtomicBoolean(false);
        JavaStream<Integer> stream = Streamable.of(1, 2, 3)
                .onClose(() -> closed.set(true));
        stream.close();
        Assertions.assertTrue(closed.get());
    }

    @Test
    void testCloseWithConcat() {
        AtomicInteger closed = new AtomicInteger();
        Streamable.of(1, 2, 3)
                .onClose(() -> closed.incrementAndGet())
                .as(AdvancedStream.AdvancedStream())
                .concat(Streamable.of(4, 5, 6).onClose(() -> closed.incrementAndGet()))
                .forEach(i -> {});
        Assertions.assertEquals(2, closed.get());
    }

    @Test
    void testCloseWithZip() {
        AtomicBoolean closed = new AtomicBoolean(false);
        Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .zip(Streamable.of(4, 5, 6).onClose(() -> closed.set(true)))
                .forEach((i) -> {});
        Assertions.assertTrue(closed.get());
    }

    @Test
    void testCloseTryWithResources() {
        AtomicBoolean closed = new AtomicBoolean(false);
        try (JavaStream<Integer> stream = Streamable.of(1, 2, 3).onClose(() -> {
            closed.set(true);
        })) {
        }
        Assertions.assertTrue(closed.get());
    }
}
