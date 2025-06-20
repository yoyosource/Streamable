package de.yoyosource.numberstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.NumberStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MinTest {

    @Test
    void testMin() {
        Optional<Integer> result = Streamable.of(1, 2, 3)
                .as(NumberStream.NumberStream())
                .min();
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1, result.get());
    }

    @Test
    void testMinNoResult() {
        Optional<Integer> result = Streamable.<Integer>of()
                .as(NumberStream.NumberStream())
                .min();
        Assertions.assertTrue(result.isEmpty());
    }
}
