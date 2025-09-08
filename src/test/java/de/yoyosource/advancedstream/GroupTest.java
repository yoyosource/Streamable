package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class GroupTest {

    @Nested
    class Sequential {
        @Test
        void testGroup() {
            Map<Integer, List<Integer>> map = Streamable.of(1, 2, 2, 3, 3, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .group()
                    .as(JavaStream.JavaStream())
                    .findFirst()
                    .orElseThrow();
            Assertions.assertEquals(3, map.size());
            Assertions.assertTrue(map.containsKey(1));
            Assertions.assertEquals(1, map.get(1).size());
            Assertions.assertEquals(1, map.get(1).get(0));
            Assertions.assertTrue(map.containsKey(2));
            Assertions.assertEquals(2, map.get(2).size());
            Assertions.assertEquals(2, map.get(2).get(0));
            Assertions.assertEquals(2, map.get(2).get(1));
            Assertions.assertTrue(map.containsKey(3));
            Assertions.assertEquals(3, map.get(3).size());
            Assertions.assertEquals(3, map.get(3).get(0));
            Assertions.assertEquals(3, map.get(3).get(1));
            Assertions.assertEquals(3, map.get(3).get(2));
        }

        @Test
        void testGroupBy() {
            Map<Boolean, List<Integer>> map = Streamable.of(1, 2, 2, 3, 3, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .groupBy(integer -> integer % 2 == 0)
                    .as(JavaStream.JavaStream())
                    .findFirst()
                    .orElseThrow();
            Assertions.assertEquals(2, map.size());
            Assertions.assertTrue(map.containsKey(true));
            Assertions.assertEquals(2, map.get(true).size());
            Assertions.assertEquals(2, map.get(true).get(0));
            Assertions.assertEquals(2, map.get(true).get(1));
            Assertions.assertTrue(map.containsKey(false));
            Assertions.assertEquals(4, map.get(false).size());
            Assertions.assertEquals(1, map.get(false).get(0));
            Assertions.assertEquals(3, map.get(false).get(1));
            Assertions.assertEquals(3, map.get(false).get(2));
            Assertions.assertEquals(3, map.get(false).get(3));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testGroup() {
            Map<Integer, List<Integer>> map = Streamable.of(1, 2, 2, 3, 3, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .group()
                    .as(JavaStream.JavaStream())
                    .findFirst()
                    .orElseThrow();
            Assertions.assertEquals(3, map.size());
            Assertions.assertTrue(map.containsKey(1));
            Assertions.assertEquals(1, map.get(1).size());
            Assertions.assertEquals(1, map.get(1).get(0));
            Assertions.assertTrue(map.containsKey(2));
            Assertions.assertEquals(2, map.get(2).size());
            Assertions.assertEquals(2, map.get(2).get(0));
            Assertions.assertEquals(2, map.get(2).get(1));
            Assertions.assertTrue(map.containsKey(3));
            Assertions.assertEquals(3, map.get(3).size());
            Assertions.assertEquals(3, map.get(3).get(0));
            Assertions.assertEquals(3, map.get(3).get(1));
            Assertions.assertEquals(3, map.get(3).get(2));
        }

        @Test
        void testGroupBy() {
            Map<Boolean, List<Integer>> map = Streamable.of(1, 2, 2, 3, 3, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .groupBy(integer -> integer % 2 == 0)
                    .as(JavaStream.JavaStream())
                    .findFirst()
                    .orElseThrow();
            Assertions.assertEquals(2, map.size());
            Assertions.assertTrue(map.containsKey(true));
            Assertions.assertEquals(2, map.get(true).size());
            Assertions.assertEquals(2, map.get(true).get(0));
            Assertions.assertEquals(2, map.get(true).get(1));
            Assertions.assertTrue(map.containsKey(false));
            Assertions.assertEquals(4, map.get(false).size());
            Assertions.assertEquals(1, map.get(false).get(0));
            Assertions.assertEquals(3, map.get(false).get(1));
            Assertions.assertEquals(3, map.get(false).get(2));
            Assertions.assertEquals(3, map.get(false).get(3));
        }
    }
}
