package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class FilterIndexedTest {

    @Test
    void testFilterIndexedNoneRemoved() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .filterIndexed((integer, index) -> integer > 0)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0));
        Assertions.assertEquals(2, list.get(1));
        Assertions.assertEquals(3, list.get(2));
    }

    @Test
    void testFilterIndexedAllRemoved() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .filterIndexed((integer, index) -> integer < 0)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(0, list.size());
    }

    @Test
    void testFilterIndexedSomeRemoved() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .filterIndexed((integer, index) -> integer > 1)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(2, list.get(0));
        Assertions.assertEquals(3, list.get(1));
    }
}
