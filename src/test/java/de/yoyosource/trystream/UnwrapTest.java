package de.yoyosource.trystream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.TryedStream;
import de.yoyosource.streamable.streams.TryingStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

class UnwrapTest {

    @Nested
    class Sequential {
        @Test
        void testUnwrapSuccessful() {
            List<Integer> list = Streamable.of("1", "a")
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .unwrap(TryedStream.successful())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertNull(list.get(1));
        }

        @Test
        void testUnwrapFailed() {
            List<RuntimeException> list = Streamable.of("1", "a")
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .unwrap(TryedStream.failed())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(2, list.size());
            Assertions.assertNull(list.get(0));
            Assertions.assertInstanceOf(NumberFormatException.class, list.get(1));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testUnwrapSuccessful() {
            List<Integer> list = Streamable.of("1", "a")
                    .parallel(3)
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .unwrap(TryedStream.successful())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertNull(list.get(1));
        }

        @Test
        void testUnwrapFailed() {
            List<RuntimeException> list = Streamable.of("1", "a")
                    .parallel(3)
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .unwrap(TryedStream.failed())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(2, list.size());
            Assertions.assertNull(list.get(0));
            Assertions.assertInstanceOf(NumberFormatException.class, list.get(1));
        }
    }
}
