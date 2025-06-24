package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class PeekIndexedTest {

    @Test
    void testPeekIndexed() {
        AtomicInteger count = new AtomicInteger();
        List<Integer> list =  Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .peekIndexed((integer, index) -> count.accumulateAndGet(integer, Integer::sum))
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(6, count.get());
        Assertions.assertEquals(1, list.get(0));
        Assertions.assertEquals(2, list.get(1));
        Assertions.assertEquals(3, list.get(2));
    }
}
