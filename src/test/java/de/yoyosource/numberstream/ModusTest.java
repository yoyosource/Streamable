package de.yoyosource.numberstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.NumberStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class ModusTest {

    @Test
    void testModus() {
        Optional<Integer> result = Streamable.of(1, 2, 3)
                .as(NumberStream.NumberStream())
                .modus();
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1, result.get());
    }

    @Test
    void testModusNoResult() {
        Optional<Integer> result = Streamable.<Integer>of()
                .as(NumberStream.NumberStream())
                .modus();
        Assertions.assertTrue(result.isEmpty());
    }
}
