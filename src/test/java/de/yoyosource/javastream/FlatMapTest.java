package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class FlatMapTest {

    @Nested
    class Sequential {
        @Test
        void testFlatMapSameSize() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .flatMap(List::of)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testFlatMapMultipleElements() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .flatMap(i -> List.of(i, i))
                    .collect(Collectors.toList());
            Assertions.assertEquals(6, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(1, list.get(1));
            Assertions.assertEquals(2, list.get(2));
            Assertions.assertEquals(2, list.get(3));
            Assertions.assertEquals(3, list.get(4));
            Assertions.assertEquals(3, list.get(5));
        }

        @Test
        void testFlatMapInfiniteElements() {
            List<Integer> list = Streamable.of(1, 2)
                    .flatMap(i -> Streamable.generate(() -> i))
                    .limit(10)
                    .collect(Collectors.toList());
            Assertions.assertEquals(10, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(1, list.get(1));
            Assertions.assertEquals(1, list.get(2));
            Assertions.assertEquals(1, list.get(3));
            Assertions.assertEquals(1, list.get(4));
            Assertions.assertEquals(1, list.get(5));
            Assertions.assertEquals(1, list.get(6));
            Assertions.assertEquals(1, list.get(7));
            Assertions.assertEquals(1, list.get(8));
            Assertions.assertEquals(1, list.get(9));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testFlatMapSameSize() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .flatMap(List::of)
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testFlatMapMultipleElements() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .flatMap(i -> List.of(i, i))
                    .collect(Collectors.toList());
            Assertions.assertEquals(6, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(1, list.get(1));
            Assertions.assertEquals(2, list.get(2));
            Assertions.assertEquals(2, list.get(3));
            Assertions.assertEquals(3, list.get(4));
            Assertions.assertEquals(3, list.get(5));
        }

        @Test
        void testFlatMapInfiniteElements() {
            List<Integer> list = Streamable.of(1, 2)
                    .parallel(3)
                    .flatMap(i -> Streamable.generate(() -> i))
                    .limit(10)
                    .collect(Collectors.toList());
            Assertions.assertEquals(10, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(1, list.get(1));
            Assertions.assertEquals(1, list.get(2));
            Assertions.assertEquals(1, list.get(3));
            Assertions.assertEquals(1, list.get(4));
            Assertions.assertEquals(1, list.get(5));
            Assertions.assertEquals(1, list.get(6));
            Assertions.assertEquals(1, list.get(7));
            Assertions.assertEquals(1, list.get(8));
            Assertions.assertEquals(1, list.get(9));
        }
    }
}
