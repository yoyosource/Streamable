package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class ToSetTest {

    @Test
    void testToSet() {
        Set<Integer> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .toSet();
        Assertions.assertEquals(3, list.size());
        Assertions.assertTrue(list.contains(1));
        Assertions.assertTrue(list.contains(2));
        Assertions.assertTrue(list.contains(3));
    }
}
