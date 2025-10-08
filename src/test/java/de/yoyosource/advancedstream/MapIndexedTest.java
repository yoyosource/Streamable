package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class MapIndexedTest {

    @Nested
    class Sequential {
        @Test
        void testMapIndexedSameType() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .mapIndexed((i, index) -> i * 2)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(2, list.get(0));
            Assertions.assertEquals(4, list.get(1));
            Assertions.assertEquals(6, list.get(2));
        }

        @Test
        void testMapIndexedTypeChange() {
            List<String> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .mapIndexed((i, index) -> i + "")
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals("1", list.get(0));
            Assertions.assertEquals("2", list.get(1));
            Assertions.assertEquals("3", list.get(2));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testMapIndexedSameType() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .mapIndexed((i, index) -> i * 2)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(2, list.get(0));
            Assertions.assertEquals(4, list.get(1));
            Assertions.assertEquals(6, list.get(2));
        }

        @Test
        void testMapIndexedTypeChange() {
            List<String> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .mapIndexed((i, index) -> i + "")
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals("1", list.get(0));
            Assertions.assertEquals("2", list.get(1));
            Assertions.assertEquals("3", list.get(2));
        }
    }
}
