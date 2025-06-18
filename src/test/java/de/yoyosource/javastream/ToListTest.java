package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ToListTest {

    @Test
    void testToList() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .toList();
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0));
        Assertions.assertEquals(2, list.get(1));
        Assertions.assertEquals(3, list.get(2));
    }
}
