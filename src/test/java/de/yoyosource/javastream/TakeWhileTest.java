package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class TakeWhileTest {

    @Nested
    class Sequential {
        @Test
        void testTakeWhileNone() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .takeWhile(integer -> false)
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void testTakeWhileAll() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .takeWhile(integer -> true)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testTakeWhileOnlyFirst() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .takeWhile(integer -> integer < 2)
                    .collect(Collectors.toList());
            Assertions.assertEquals(1, list.size());
            Assertions.assertEquals(1, list.get(0));
        }

        @Test
        void testTakeWhileOnlyLast() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .takeWhile(integer -> integer > 2)
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testTakeWhileNone() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .takeWhile(integer -> false)
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void testTakeWhileAll() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .takeWhile(integer -> true)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test // TODO: Flaky?
        void testTakeWhileOnlyFirst() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .takeWhile(integer -> integer < 2)
                    .collect(Collectors.toList());
            Assertions.assertEquals(1, list.size());
            Assertions.assertEquals(1, list.get(0));
        }

        @Test
        void testTakeWhileOnlyLast() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .takeWhile(integer -> integer > 2)
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }
    }
}
