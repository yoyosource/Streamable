package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class SortedTest {

    @Nested
    class Sequential {
        @Test
        void testSorted() {
            List<Integer> list = Streamable.of(3, 2, 1)
                    .sorted(Integer::compareTo)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }
    }

    @Nested
    class Parallel {
        @Test // TODO Flaky?
        void testSorted() {
            List<Integer> list = Streamable.of(3, 2, 1)
                    .parallel(3)
                    .sorted(Integer::compareTo)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }
    }
}
