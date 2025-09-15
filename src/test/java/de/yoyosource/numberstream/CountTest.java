package de.yoyosource.numberstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.NumberStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CountTest {

    @Nested
    class Sequential {
        @Test
        void testCount() {
            long result = Streamable.of(1, 2, 3)
                    .as(NumberStream.NumberStream())
                    .count();
            Assertions.assertEquals(3, result);
        }

        @Test
        void testCountNoResult() {
            long result = Streamable.<Integer>of()
                    .as(NumberStream.NumberStream())
                    .count();
            Assertions.assertEquals(0, result);
        }
    }

    @Nested
    class Parallel {
        @Test
        void testCount() {
            long result = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(NumberStream.NumberStream())
                    .count();
            Assertions.assertEquals(3, result);
        }

        @Test
        void testCountNoResult() {
            long result = Streamable.<Integer>of()
                    .parallel(3)
                    .as(NumberStream.NumberStream())
                    .count();
            Assertions.assertEquals(0, result);
        }
    }
}
