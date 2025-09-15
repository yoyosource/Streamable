package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MinTest {

    @Nested
    class Sequential {
        @Test
        void testMin() {
            Optional<Integer> result = Streamable.of(3, 2, 1)
                    .min(Integer::compareTo);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(1, result.get());
        }

        @Test
        void testMinNoElements() {
            Optional<Integer> result = Streamable.<Integer>of()
                    .min(Integer::compareTo);
            Assertions.assertTrue(result.isEmpty());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testMin() {
            Optional<Integer> result = Streamable.of(3, 2, 1)
                    .parallel(3)
                    .min(Integer::compareTo);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(1, result.get());
        }

        @Test
        void testMinNoElements() {
            Optional<Integer> result = Streamable.<Integer>of()
                    .parallel(3)
                    .min(Integer::compareTo);
            Assertions.assertTrue(result.isEmpty());
        }
    }
}
