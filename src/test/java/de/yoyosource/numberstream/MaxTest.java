package de.yoyosource.numberstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.NumberStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MaxTest {

    @Test
    void testMax() {
        Optional<Integer> result = Streamable.of(1, 2, 3)
                .as(NumberStream.NumberStream())
                .max();
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(3, result.get());
    }

    @Test
    void testMaxNoResult() {
        Optional<Integer> result = Streamable.<Integer>of()
                .as(NumberStream.NumberStream())
                .max();
        Assertions.assertTrue(result.isEmpty());
    }
}
