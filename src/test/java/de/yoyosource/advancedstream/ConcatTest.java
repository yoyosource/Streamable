package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class ConcatTest {

    @Nested
    class Sequential {
        @Test
        void testConcat() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .concat(Streamable.of(4, 5, 6))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(6, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
            Assertions.assertEquals(4, list.get(3));
            Assertions.assertEquals(5, list.get(4));
            Assertions.assertEquals(6, list.get(5));
        }

        @Test
        void testConcatNoElementsInFirst() {
            List<Integer> list = Streamable.<Integer>of()
                    .as(AdvancedStream.AdvancedStream())
                    .concat(Streamable.of(4, 5, 6))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(4, list.get(0));
            Assertions.assertEquals(5, list.get(1));
            Assertions.assertEquals(6, list.get(2));
        }

        @Test
        void testConcatNoElementsInSecond() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .concat(Streamable.of())
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testConcatMulti() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .concat(Streamable.of(4, 5, 6))
                    .concat(Streamable.of(7, 8, 9))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(9, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
            Assertions.assertEquals(4, list.get(3));
            Assertions.assertEquals(5, list.get(4));
            Assertions.assertEquals(6, list.get(5));
            Assertions.assertEquals(7, list.get(6));
            Assertions.assertEquals(8, list.get(7));
            Assertions.assertEquals(9, list.get(8));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testConcat() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .concat(Streamable.of(4, 5, 6))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(6, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
            Assertions.assertEquals(4, list.get(3));
            Assertions.assertEquals(5, list.get(4));
            Assertions.assertEquals(6, list.get(5));
        }

        @Test
        void testConcatNoElementsInFirst() {
            List<Integer> list = Streamable.<Integer>of()
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .concat(Streamable.of(4, 5, 6))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(4, list.get(0));
            Assertions.assertEquals(5, list.get(1));
            Assertions.assertEquals(6, list.get(2));
        }

        @Test
        void testConcatNoElementsInSecond() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .concat(Streamable.of())
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testConcatMulti() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .concat(Streamable.of(4, 5, 6))
                    .concat(Streamable.of(7, 8, 9))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(9, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
            Assertions.assertEquals(4, list.get(3));
            Assertions.assertEquals(5, list.get(4));
            Assertions.assertEquals(6, list.get(5));
            Assertions.assertEquals(7, list.get(6));
            Assertions.assertEquals(8, list.get(7));
            Assertions.assertEquals(9, list.get(8));
        }
    }
}
