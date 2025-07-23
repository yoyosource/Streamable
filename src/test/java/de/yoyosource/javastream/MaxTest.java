package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MaxTest {

    @Test
    void testMax() {
        Optional<Integer> result = Streamable.of(1, 2, 3)
                .max(Integer::compareTo);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(3, result.get());
    }

    @Test
    void testMaxNoElements() {
        Optional<Integer> result = Streamable.<Integer>of()
                .max(Integer::compareTo);
        Assertions.assertTrue(result.isEmpty());
    }
}
