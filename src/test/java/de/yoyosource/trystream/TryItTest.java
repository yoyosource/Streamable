package de.yoyosource.trystream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.Try;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.TryingStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

class TryItTest {

    @Nested
    class Sequential {
        @Test
        void testTryIt() {
            List<Try<Integer, RuntimeException>> result = Streamable.of("1", "a")
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(2, result.size());
            Assertions.assertTrue(result.get(0).successful());
            Assertions.assertTrue(result.get(1).failed());
            Assertions.assertEquals(1, result.get(0).getSuccess());
        }

        @Test
        void testTryItEndOnError() {
            List<Try<Integer, RuntimeException>> result = Streamable.of("1", "a", "0")
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt, true)
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(1, result.size());
            Assertions.assertTrue(result.get(0).successful());
            Assertions.assertEquals(1, result.get(0).getSuccess());
        }

        @Test
        void testTryItTwice() {
            List<Try<Integer, RuntimeException>> result = Streamable.of("1", "a", "0")
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .tryIt(integer -> 1 / integer)
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(3, result.size());
            Assertions.assertTrue(result.get(0).successful());
            Assertions.assertTrue(result.get(1).failed());
            Assertions.assertTrue(result.get(2).failed());
            Assertions.assertEquals(1, result.get(0).getSuccess());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testTryIt() {
            List<Try<Integer, RuntimeException>> result = Streamable.of("1", "a")
                    .parallel(3)
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(2, result.size());
            Assertions.assertTrue(result.get(0).successful());
            Assertions.assertTrue(result.get(1).failed());
            Assertions.assertEquals(1, result.get(0).getSuccess());
        }

        @Test
        void testTryItEndOnError() {
            List<Try<Integer, RuntimeException>> result = Streamable.of("1", "a", "0")
                    .parallel(3)
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt, true)
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(1, result.size());
            Assertions.assertTrue(result.get(0).successful());
            Assertions.assertEquals(1, result.get(0).getSuccess());
        }

        @Test
        void testTryItTwice() {
            List<Try<Integer, RuntimeException>> result = Streamable.of("1", "a", "0")
                    .parallel(3)
                    .as(TryingStream.TryingStream())
                    .tryIt(Integer::parseInt)
                    .tryIt(integer -> 1 / integer)
                    .as(JavaStream.JavaStream())
                    .toList();

            Assertions.assertEquals(3, result.size());
            Assertions.assertTrue(result.get(0).successful());
            Assertions.assertTrue(result.get(1).failed());
            Assertions.assertTrue(result.get(2).failed());
            Assertions.assertEquals(1, result.get(0).getSuccess());
        }
    }
}
