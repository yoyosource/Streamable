package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CountTest {

    @Nested
    class Sequential {
        @Test
        void testCount() {
            Map<Integer, Long> map = Streamable.of(1, 2, 1, 2, 3, 1)
                    .as(AdvancedStream.AdvancedStream())
                    .count()
                    .as(JavaStream.JavaStream())
                    .findFirst()
                    .orElseThrow();
            Assertions.assertEquals(3, map.size());
            Assertions.assertTrue(map.containsKey(1));
            Assertions.assertEquals(3, map.get(1));
            Assertions.assertTrue(map.containsKey(2));
            Assertions.assertEquals(2, map.get(2));
            Assertions.assertTrue(map.containsKey(3));
            Assertions.assertEquals(1, map.get(3));
        }

        @Test
        void testCountBy() {
            Map<Integer, Long> map = Streamable.of(1, 2, 1, 2, 3, 1)
                    .as(AdvancedStream.AdvancedStream())
                    .countBy(integer -> integer / 2)
                    .as(JavaStream.JavaStream())
                    .findFirst()
                    .orElseThrow();
            Assertions.assertEquals(2, map.size());
            Assertions.assertTrue(map.containsKey(0));
            Assertions.assertEquals(3, map.get(0));
            Assertions.assertTrue(map.containsKey(1));
            Assertions.assertEquals(3, map.get(1));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testCount() {
            Map<Integer, Long> map = Streamable.of(1, 2, 1, 2, 3, 1)
                    .as(AdvancedStream.AdvancedStream())
                    .count()
                    .as(JavaStream.JavaStream())
                    .findFirst()
                    .orElseThrow();
            Assertions.assertEquals(3, map.size());
            Assertions.assertTrue(map.containsKey(1));
            Assertions.assertEquals(3, map.get(1));
            Assertions.assertTrue(map.containsKey(2));
            Assertions.assertEquals(2, map.get(2));
            Assertions.assertTrue(map.containsKey(3));
            Assertions.assertEquals(1, map.get(3));
        }

        @Test
        void testCountBy() {
            Map<Integer, Long> map = Streamable.of(1, 2, 1, 2, 3, 1)
                    .as(AdvancedStream.AdvancedStream())
                    .countBy(integer -> integer / 2)
                    .as(JavaStream.JavaStream())
                    .findFirst()
                    .orElseThrow();
            Assertions.assertEquals(2, map.size());
            Assertions.assertTrue(map.containsKey(0));
            Assertions.assertEquals(3, map.get(0));
            Assertions.assertTrue(map.containsKey(1));
            Assertions.assertEquals(3, map.get(1));
        }
    }
}
