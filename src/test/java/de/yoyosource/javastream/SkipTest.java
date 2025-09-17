package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class SkipTest {

    @Nested
    class Sequential {
        @Test
        void testSkipOfZero() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .skip(0)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testSkipBelowZero() {
            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                Streamable.of(1, 2, 3)
                        .skip(-1)
                        .collect(Collectors.toList());
            });
            Assertions.assertEquals("Skip cannot be negative!", exception.getMessage());
        }

        @Test
        void testSkipLowNumber() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .skip(1)
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(2, list.get(0));
            Assertions.assertEquals(3, list.get(1));
        }

        @Test
        void testSkipHighNumber() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .skip(10)
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testSkipOfZero() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .skip(0)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testSkipBelowZero() {
            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                Streamable.of(1, 2, 3)
                        .parallel(3)
                        .skip(-1)
                        .collect(Collectors.toList());
            });
            Assertions.assertEquals("Skip cannot be negative!", exception.getMessage());
        }

        @Test // TODO Flaky?
        void testSkipLowNumber() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .skip(1)
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(2, list.get(0));
            Assertions.assertEquals(3, list.get(1));
        }

        @Test
        void testSkipHighNumber() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .skip(10)
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }
    }
}
