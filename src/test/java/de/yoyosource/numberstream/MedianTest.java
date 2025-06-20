package de.yoyosource.numberstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.NumberStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MedianTest {

    @Test
    void testMedian() {
        Optional<Integer> result = Streamable.of(1, 2, 3)
                .as(NumberStream.NumberStream())
                .median();
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(2, result.get());
    }

    @Test
    void testMedianNoResult() {
        Optional<Integer> result = Streamable.<Integer>of()
                .as(NumberStream.NumberStream())
                .median();
        Assertions.assertTrue(result.isEmpty());
    }
}
