package de.yoyosource.zippedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class GetLeftTest {

    @Test
    void testGetLeft() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .zip(Streamable.of(4, 5, 6))
                .getLeft()
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0));
        Assertions.assertEquals(2, list.get(1));
        Assertions.assertEquals(3, list.get(2));
    }

    @Test
    void testGetLeftNull() {
        List<Integer> list = Streamable.<Integer>of()
                .as(AdvancedStream.AdvancedStream())
                .zip(Streamable.of(4, 5, 6))
                .getLeft()
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertNull(list.get(0));
        Assertions.assertNull(list.get(1));
        Assertions.assertNull(list.get(2));
    }
}
