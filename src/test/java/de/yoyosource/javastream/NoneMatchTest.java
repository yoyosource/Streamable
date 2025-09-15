package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NoneMatchTest {

    @Nested
    class Sequential {
        @Test
        void testNoneMatchMatches() {
            boolean result = Streamable.of(1, 2, 3)
                    .noneMatch(integer -> integer == 1);
            Assertions.assertFalse(result);
        }

        @Test
        void testNoneMatchNoMatch() {
            boolean result = Streamable.of(1, 2, 3)
                    .noneMatch(integer -> integer == 4);
            Assertions.assertTrue(result);
        }
    }

    @Nested
    class Parallel {
        @Test
        @Disabled
        void testNoneMatchMatches() {
            boolean result = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .noneMatch(integer -> integer == 1);
            Assertions.assertFalse(result);
        }

        @Test
        void testNoneMatchNoMatch() {
            boolean result = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .noneMatch(integer -> integer == 4);
            Assertions.assertTrue(result);
        }
    }
}
