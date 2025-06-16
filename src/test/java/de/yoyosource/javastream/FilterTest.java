package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class FilterTest {

    @Test
    void filterNoneRemoved() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .filter(integer -> integer > 0)
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0));
        Assertions.assertEquals(2, list.get(1));
        Assertions.assertEquals(3, list.get(2));
    }

    @Test
    void filterAllRemoved() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .filter(integer -> integer < 0)
                .collect(Collectors.toList());
        Assertions.assertEquals(0, list.size());
    }

    @Test
    void filterSomeRemoved() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .filter(integer -> integer > 1)
                .collect(Collectors.toList());
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(2, list.get(0));
        Assertions.assertEquals(3, list.get(1));
    }
}
