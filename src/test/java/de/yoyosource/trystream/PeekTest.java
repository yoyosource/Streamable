package de.yoyosource.trystream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.Try;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.TryedStream;
import de.yoyosource.streamable.streams.TryingStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class PeekTest {

    @Test
    void testPeekSuccessful() {
        AtomicInteger counter = new AtomicInteger(0);
        List<Try<Integer, RuntimeException>> list = Streamable.of("1", "a")
                .as(TryingStream.TryingStream())
                .tryIt(Integer::parseInt)
                .peek(TryedStream.successful(), integer -> {
                    counter.incrementAndGet();
                })
                .as(JavaStream.JavaStream())
                .toList();

        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(1, counter.get());
    }

    @Test
    void testPeekFailed() {
        AtomicInteger counter = new AtomicInteger(0);
        List<Try<Integer, RuntimeException>> list = Streamable.of("1", "a")
                .as(TryingStream.TryingStream())
                .tryIt(Integer::parseInt)
                .peek(TryedStream.failed(), integer -> {
                    counter.incrementAndGet();
                })
                .as(JavaStream.JavaStream())
                .toList();

        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(1, counter.get());
    }
}
