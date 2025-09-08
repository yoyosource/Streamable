package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class TakeWhileIndexedTest {

    @Nested
    class Sequential {
        @Test
        void testTakeWhileIndexedNone() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .takeWhileIndexed((integer, index) -> false)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void testTakeWhileIndexedAll() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .takeWhileIndexed((integer, index) -> true)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testTakeWhileIndexedOnlyFirst() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .takeWhileIndexed((integer, index) -> integer < 2)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(1, list.size());
            Assertions.assertEquals(1, list.get(0));
        }

        @Test
        void testTakeWhileIndexedOnlyLast() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .takeWhileIndexed((integer, index) -> integer > 2)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testTakeWhileIndexedNone() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .takeWhileIndexed((integer, index) -> false)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void testTakeWhileIndexedAll() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .takeWhileIndexed((integer, index) -> true)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testTakeWhileIndexedOnlyFirst() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .takeWhileIndexed((integer, index) -> integer < 2)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(1, list.size());
            Assertions.assertEquals(1, list.get(0));
        }

        @Test
        void testTakeWhileIndexedOnlyLast() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .takeWhileIndexed((integer, index) -> integer > 2)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }
    }
}
