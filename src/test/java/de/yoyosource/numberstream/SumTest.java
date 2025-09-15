package de.yoyosource.numberstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.NumberStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class SumTest {

    @Nested
    class Sequential {
        @Test
        void testSum() {
            Optional<Integer> result = Streamable.of(1, 2, 3)
                    .as(NumberStream.NumberStream())
                    .sum();
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(6, result.get());
        }

        @Test
        void testSumNoResult() {
            Optional<Integer> result = Streamable.<Integer>of()
                    .as(NumberStream.NumberStream())
                    .sum();
            Assertions.assertTrue(result.isEmpty());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testSum() {
            Optional<Integer> result = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(NumberStream.NumberStream())
                    .sum();
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(6, result.get());
        }

        @Test
        void testSumNoResult() {
            Optional<Integer> result = Streamable.<Integer>of()
                    .parallel(3)
                    .as(NumberStream.NumberStream())
                    .sum();
            Assertions.assertTrue(result.isEmpty());
        }
    }
}
