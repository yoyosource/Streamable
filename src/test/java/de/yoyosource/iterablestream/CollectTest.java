package de.yoyosource.iterablestream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.IterableStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class CollectTest {

    @Test
    void testCollectEach() {
        List<List<Integer>> list = Streamable.of(List.of(1, 2, 3), Streamable.of(4, 5, 6))
                .as(IterableStream.IterableStream())
                .collect(Collectors.toList())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(3, list.get(0).size());
        Assertions.assertEquals(1, list.get(0).get(0));
        Assertions.assertEquals(2, list.get(0).get(1));
        Assertions.assertEquals(3, list.get(0).get(2));
        Assertions.assertEquals(3, list.get(1).size());
        Assertions.assertEquals(4, list.get(1).get(0));
        Assertions.assertEquals(5, list.get(1).get(1));
        Assertions.assertEquals(6, list.get(1).get(2));
    }
}
