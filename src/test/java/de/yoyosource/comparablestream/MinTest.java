package de.yoyosource.comparablestream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.ComparableStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MinTest {

    @Test
    void testMin() {
        Optional<Integer> result = Streamable.of(1, 2, 3)
                .as(ComparableStream.ComparableStream())
                .min();
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1, result.get());
    }

    @Test
    void testMinNoElement() {
        Optional<Integer> result = Streamable.<Integer>of()
                .as(ComparableStream.ComparableStream())
                .min();
        Assertions.assertTrue(result.isEmpty());
    }
}
