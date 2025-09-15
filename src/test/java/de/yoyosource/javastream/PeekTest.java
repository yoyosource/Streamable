package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class PeekTest {

    @Nested
    class Sequential {
        @Test
        void testPeek() {
            AtomicInteger count = new AtomicInteger();
            List<Integer> list = Streamable.of(1, 2, 3)
                    .peek(integer -> count.accumulateAndGet(integer, Integer::sum))
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(6, count.get());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testPeek() {
            AtomicInteger count = new AtomicInteger();
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .peek(integer -> count.accumulateAndGet(integer, Integer::sum))
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(6, count.get());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }
    }
}
