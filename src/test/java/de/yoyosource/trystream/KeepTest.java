package de.yoyosource.trystream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.Try;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.TryedStream;
import de.yoyosource.streamable.streams.TryingStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

class KeepTest {

    @Nested
    class Sequential {
        @Test
        void testKeepSuccessful() {
            List<Try<Integer, RuntimeException>> list = Streamable.of("1", "a")
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .keep(TryedStream.successful())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(1, list.size());
            Assertions.assertTrue(list.get(0).successful());
            Assertions.assertEquals(1, list.get(0).getSuccess());
        }

        @Test
        void testKeepFailed() {
            List<Try<Integer, RuntimeException>> list = Streamable.of("1", "a")
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .keep(TryedStream.failed())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(1, list.size());
            Assertions.assertTrue(list.get(0).failed());
            Assertions.assertInstanceOf(NumberFormatException.class, list.get(0).getFailure());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testKeepSuccessful() {
            List<Try<Integer, RuntimeException>> list = Streamable.of("1", "a")
                    .parallel(3)
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .keep(TryedStream.successful())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(1, list.size());
            Assertions.assertTrue(list.get(0).successful());
            Assertions.assertEquals(1, list.get(0).getSuccess());
        }

        @Test
        void testKeepFailed() {
            List<Try<Integer, RuntimeException>> list = Streamable.of("1", "a")
                    .parallel(3)
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .keep(TryedStream.failed())
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(1, list.size());
            Assertions.assertTrue(list.get(0).failed());
            Assertions.assertInstanceOf(NumberFormatException.class, list.get(0).getFailure());
        }
    }
}
