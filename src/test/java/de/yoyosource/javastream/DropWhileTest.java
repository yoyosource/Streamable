package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class DropWhileTest {

    @Nested
    class Sequential {
        @Test
        void testDropWhileNone() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .dropWhile(integer -> false)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testDropWhileAll() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .dropWhile(integer -> true)
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void testDropWhileOnlyFirst() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .dropWhile(integer -> integer < 2)
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(2, list.get(0));
            Assertions.assertEquals(3, list.get(1));
        }

        @Test
        void testDropWhileOnlyLast() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .dropWhile(integer -> integer > 2)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testDropWhileNone() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .dropWhile(integer -> false)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testDropWhileAll() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .dropWhile(integer -> true)
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void testDropWhileOnlyFirst() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .dropWhile(integer -> integer < 2)
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(2, list.get(0));
            Assertions.assertEquals(3, list.get(1));
        }

        @Test
        void testDropWhileOnlyLast() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .dropWhile(integer -> integer > 2)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }
    }
}
