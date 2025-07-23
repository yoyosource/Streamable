package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MinTest {

    @Test
    void testMin() {
        Optional<Integer> result = Streamable.of(3, 2, 1)
                .min(Integer::compareTo);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1, result.get());
    }

    @Test
    void testMinNoElements() {
        Optional<Integer> result = Streamable.<Integer>of()
                .min(Integer::compareTo);
        Assertions.assertTrue(result.isEmpty());
    }
}
