package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class FindLastTest {

    @Nested
    class Sequential {
        @Test
        void testFindLast() {
            Optional<Integer> result = Streamable.of(1, 2, 3)
                    .findLast();
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(3, result.get());
        }

        @Test
        void testFindLastNoResult() {
            Optional<Integer> result = Streamable.<Integer>of()
                    .findLast();
            Assertions.assertTrue(result.isEmpty());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testFindLast() {
            Optional<Integer> result = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .findLast();
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(3, result.get());
        }

        @Test
        void testFindLastNoResult() {
            Optional<Integer> result = Streamable.<Integer>of()
                    .parallel(3)
                    .findLast();
            Assertions.assertTrue(result.isEmpty());
        }
    }
}
