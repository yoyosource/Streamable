package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class FindAnyTest {

    @Nested
    class Sequential {
        @Test
        void testFindAny() {
            Optional<Integer> result = Streamable.of(1, 2, 3)
                    .findAny();
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(1, result.get());
        }

        @Test
        void testFindAnyNoResult() {
            Optional<Integer> result = Streamable.<Integer>of()
                    .findAny();
            Assertions.assertTrue(result.isEmpty());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testFindAny() {
            Optional<Integer> result = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .findAny();
            Assertions.assertTrue(result.isPresent());
        }

        @Test
        void testFindAnyNoResult() {
            Optional<Integer> result = Streamable.<Integer>of()
                    .parallel(3)
                    .findAny();
            Assertions.assertTrue(result.isEmpty());
        }
    }
}
