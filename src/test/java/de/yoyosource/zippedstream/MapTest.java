package de.yoyosource.zippedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class MapTest {

    @Nested
    class Sequential {
        @Test
        void testMap() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .map(Integer::sum)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(5, list.get(0));
            Assertions.assertEquals(7, list.get(1));
            Assertions.assertEquals(9, list.get(2));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testMap() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .map(Integer::sum)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(5, list.get(0));
            Assertions.assertEquals(7, list.get(1));
            Assertions.assertEquals(9, list.get(2));
        }
    }
}
