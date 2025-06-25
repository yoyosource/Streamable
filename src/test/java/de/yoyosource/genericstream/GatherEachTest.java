package de.yoyosource.genericstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.GenericStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class GatherEachTest {

    @Test
    void testGatherEach() {
        List<Integer> list = Streamable.of(List.of(1, 2, 3), Streamable.of(4, 5, 6))
                .map(Streamable::from)
                .as(GenericStream.GenericStream())
                .gatherEach(integers -> integers.map(integer -> integer * 2))
                .flatten()
                .collect(Collectors.toList());
        Assertions.assertEquals(6, list.size());
        Assertions.assertEquals(2, list.get(0));
        Assertions.assertEquals(4, list.get(1));
        Assertions.assertEquals(6, list.get(2));
        Assertions.assertEquals(8, list.get(3));
        Assertions.assertEquals(10, list.get(4));
        Assertions.assertEquals(12, list.get(5));
    }
}
