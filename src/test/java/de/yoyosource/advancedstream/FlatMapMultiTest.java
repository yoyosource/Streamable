package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class FlatMapMultiTest {

    @Nested
    class Sequential {
        @Test
        void testFlatMapMultiIndexedSameSize() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>flatMapMulti((integer, consumer) -> consumer.accept(List.of(integer)))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testFlatMapMultiIndexedMultipleElements() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>flatMapMulti((integer, consumer) -> {
                        consumer.accept(List.of(integer, integer));
                    })
                    .as(JavaStream.JavaStream())
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
        void testFlatMapMultiIndexedInfiniteElements() {
            List<Integer> list = Streamable.of(1, 2)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>flatMapMulti((integer, consumer) -> {
                        consumer.accept(Streamable.generate(() -> integer));
                    })
                    .as(JavaStream.JavaStream())
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

        @Test
        void testFlatMapMultiIndexedInfiniteLoop() {
            List<Integer> list = Streamable.of(1, 2)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>flatMapMulti((integer, consumer) -> {
                        while (true) {
                            consumer.accept(List.of(integer));
                        }
                    })
                    .as(JavaStream.JavaStream())
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
        void testFlatMapMultiIndexedSameSize() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>flatMapMulti((integer, consumer) -> consumer.accept(List.of(integer)))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testFlatMapMultiIndexedMultipleElements() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>flatMapMulti((integer, consumer) -> {
                        consumer.accept(List.of(integer, integer));
                    })
                    .as(JavaStream.JavaStream())
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
        void testFlatMapMultiIndexedInfiniteElements() {
            List<Integer> list = Streamable.of(1, 2)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>flatMapMulti((integer, consumer) -> {
                        consumer.accept(Streamable.generate(() -> integer));
                    })
                    .as(JavaStream.JavaStream())
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

        @Test
        void testFlatMapMultiIndexedInfiniteLoop() {
            List<Integer> list = Streamable.of(1, 2)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>flatMapMulti((integer, consumer) -> {
                        while (true) {
                            consumer.accept(List.of(integer));
                        }
                    })
                    .as(JavaStream.JavaStream())
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
