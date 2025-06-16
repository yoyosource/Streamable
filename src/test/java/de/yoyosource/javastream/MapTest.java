package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class MapTest {

    @Test
    void testMapSameType() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .map(i -> i * 2)
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(2, list.get(0));
        Assertions.assertEquals(4, list.get(1));
        Assertions.assertEquals(6, list.get(2));
    }

    @Test
    void testMapTypeChange() {
        List<String> list = Streamable.of(1, 2, 3)
                .map(i -> i + "")
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals("1", list.get(0));
        Assertions.assertEquals("2", list.get(1));
        Assertions.assertEquals("3", list.get(2));
    }
}
