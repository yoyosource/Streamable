package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class LimitTest {

    @Test
    void testLimitOfZero() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .limit(0)
                .collect(Collectors.toList());
        Assertions.assertEquals(0, list.size());
    }

    @Test
    void testLimitBelowZero() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Streamable.of(1, 2, 3)
                    .limit(-1)
                    .collect(Collectors.toList());
        });
        Assertions.assertEquals("Size cannot be negative!", exception.getMessage());
    }

    @Test
    void testLimitLowNumber() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .limit(1)
                .collect(Collectors.toList());
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(1, list.get(0));
    }

    @Test
    void testLimitHighNumber() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .limit(10)
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0));
        Assertions.assertEquals(2, list.get(1));
        Assertions.assertEquals(3, list.get(2));
    }
}
