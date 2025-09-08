package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class DistinctByTest {

    @Nested
    class Sequential {
        @Test
        void testCountBy() {
            List<Integer> list = Streamable.of(1, 2, 1, 2, 3, 1)
                    .as(AdvancedStream.AdvancedStream())
                    .distinctBy(integer -> integer % 2 == 0)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testCountBy() {
            List<Integer> list = Streamable.of(1, 2, 1, 2, 3, 1)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .distinctBy(integer -> integer % 2 == 0)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
        }
    }
}
