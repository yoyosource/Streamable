package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.stream.Collectors;

class MapMultiTest {

    @Test
    void testMapMultiSameSize() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .<Integer>mapMulti((integer, consumer) -> consumer.accept(integer))
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0));
        Assertions.assertEquals(2, list.get(1));
        Assertions.assertEquals(3, list.get(2));
    }

    @Test
    void testMapMultiMultipleElements() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .<Integer>mapMulti((integer, consumer) -> {
                    consumer.accept(integer);
                    consumer.accept(integer);
                })
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
    void testMapMultiInfiniteElements() {
        List<Integer> list = Streamable.of(1, 2)
                .<Integer>mapMulti((integer, consumer) -> {
                    while (true) {
                        consumer.accept(integer);
                    }
                })
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
