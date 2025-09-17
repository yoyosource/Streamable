package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AnyMatchTest {

    @Nested
    class Sequential {
        @Test
        void testAnyMatchMatches() {
            boolean result = Streamable.of(1, 2, 3)
                    .anyMatch(integer -> integer == 1);
            Assertions.assertTrue(result);
        }

        @Test
        void testAnyMatchNoMatch() {
            boolean result = Streamable.of(1, 2, 3)
                    .anyMatch(integer -> integer == 4);
            Assertions.assertFalse(result);
        }
    }

    @Nested
    class Parallel {
        @Test
        @Disabled
        void testAnyMatchMatches() {
            boolean result = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .anyMatch(integer -> integer == 1);
            Assertions.assertTrue(result);
        }

        @Test
        void testAnyMatchNoMatch() {
            boolean result = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .anyMatch(integer -> integer == 4);
            Assertions.assertFalse(result);
        }
    }
}
