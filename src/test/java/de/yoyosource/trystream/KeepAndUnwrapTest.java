package de.yoyosource.trystream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.TryedStream;
import de.yoyosource.streamable.streams.TryingStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

class KeepAndUnwrapTest {

    @Nested
    class Sequential {
        @Test
        void testKeepAndUnwrapSuccessful() {
            List<Integer> list = Streamable.of("1", "a")
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .keepAndUnwrap(TryedStream.successful())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(1, list.size());
            Assertions.assertEquals(1, list.get(0));
        }

        @Test
        void testKeepAndUnwrapFailed() {
            List<RuntimeException> list = Streamable.of("1", "a")
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .keepAndUnwrap(TryedStream.failed())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(1, list.size());
            Assertions.assertInstanceOf(NumberFormatException.class, list.get(0));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testKeepAndUnwrapSuccessful() {
            List<Integer> list = Streamable.of("1", "a")
                    .parallel(3)
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .keepAndUnwrap(TryedStream.successful())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(1, list.size());
            Assertions.assertEquals(1, list.get(0));
        }

        @Test
        void testKeepAndUnwrapFailed() {
            List<RuntimeException> list = Streamable.of("1", "a")
                    .parallel(3)
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .keepAndUnwrap(TryedStream.failed())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(1, list.size());
            Assertions.assertInstanceOf(NumberFormatException.class, list.get(0));
        }
    }
}
