package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MaxTest {

    @Nested
    class Sequential {
        @Test
        void testMax() {
            Optional<Integer> result = Streamable.of(1, 2, 3)
                    .max(Integer::compareTo);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(3, result.get());
        }

        @Test
        void testMaxNoElements() {
            Optional<Integer> result = Streamable.<Integer>of()
                    .max(Integer::compareTo);
            Assertions.assertTrue(result.isEmpty());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testMax() {
            Optional<Integer> result = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .max(Integer::compareTo);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(3, result.get());
        }

        @Test
        void testMaxNoElements() {
            Optional<Integer> result = Streamable.<Integer>of()
                    .parallel(3)
                    .max(Integer::compareTo);
            Assertions.assertTrue(result.isEmpty());
        }
    }
}
