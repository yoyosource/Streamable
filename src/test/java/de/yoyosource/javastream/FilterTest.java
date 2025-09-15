package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class FilterTest {

    @Nested
    class Sequential {
        @Test
        void testFilterNoneRemoved() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .filter(integer -> integer > 0)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testFilterAllRemoved() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .filter(integer -> integer < 0)
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void testFilterSomeRemoved() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .filter(integer -> integer > 1)
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(2, list.get(0));
            Assertions.assertEquals(3, list.get(1));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testFilterNoneRemoved() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .filter(integer -> integer > 0)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testFilterAllRemoved() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .filter(integer -> integer < 0)
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void testFilterSomeRemoved() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .filter(integer -> integer > 1)
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(2, list.get(0));
            Assertions.assertEquals(3, list.get(1));
        }
    }
}
