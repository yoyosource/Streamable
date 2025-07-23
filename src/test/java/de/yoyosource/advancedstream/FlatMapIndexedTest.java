package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class FlatMapIndexedTest {

    @Test
    void testFlatMapIndexedSameSize() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .flatMapIndexed((i, index) -> List.of(i))
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0));
        Assertions.assertEquals(2, list.get(1));
        Assertions.assertEquals(3, list.get(2));
    }

    @Test
    void testFlatMapIndexedMultipleElements() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .flatMapIndexed((i, index) -> List.of(i, i))
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
    void testFlatMapIndexedInfiniteElements() {
        List<Integer> list = Streamable.of(1, 2)
                .as(AdvancedStream.AdvancedStream())
                .flatMapIndexed((i, index) -> Streamable.generate(() -> i))
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
