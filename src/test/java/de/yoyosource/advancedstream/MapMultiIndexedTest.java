package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class MapMultiIndexedTest {

    @Nested
    class Sequential {
        @Test
        void testMapMultiIndexedSameSize() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>mapMultiIndexed((integer, index, consumer) -> consumer.accept(integer))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void testMapMultiIndexedMultipleElements() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>mapMultiIndexed((integer, index, consumer) -> {
                        consumer.accept(integer);
                        consumer.accept(integer);
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
        void testMapMultiIndexedInfiniteElements() {
            List<Integer> list = Streamable.of(1, 2)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>mapMultiIndexed((integer, index, consumer) -> {
                        while (true) {
                            consumer.accept(integer);
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
        void testMapMultiIndexedSameSize() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>mapMultiIndexed((integer, index, consumer) -> consumer.accept(integer))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
            // TODO: See why this does not work
        void testMapMultiIndexedMultipleElements() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>mapMultiIndexed((integer, index, consumer) -> {
                        consumer.accept(integer);
                        consumer.accept(integer);
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
        @Disabled // Infinite Loop
        void testMapMultiIndexedInfiniteElements() {
            List<Integer> list = Streamable.of(1, 2)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .<Integer>mapMultiIndexed((integer, index, consumer) -> {
                        while (true) {
                            consumer.accept(integer);
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
