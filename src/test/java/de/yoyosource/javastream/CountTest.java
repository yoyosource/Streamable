package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CountTest {

    @Nested
    class Sequential {
        @Test
        void testCount() {
            long count = Streamable.of(1, 2, 3)
                    .count();
            Assertions.assertEquals(3, count);
        }

        @Test
        void testCountNoElements() {
            long count = Streamable.of()
                    .count();
            Assertions.assertEquals(0, count);
        }
    }

    @Nested
    class Parallel {
        @Test
        void testCount() {
            long count = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .count();
            Assertions.assertEquals(3, count);
        }

        @Test
        void testCountNoElements() {
            long count = Streamable.of()
                    .parallel(3)
                    .count();
            Assertions.assertEquals(0, count);
        }
    }
}
