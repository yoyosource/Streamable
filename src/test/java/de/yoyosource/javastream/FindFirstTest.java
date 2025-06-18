package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class FindFirstTest {

    @Test
    void testFindFirst() {
        Optional<Integer> result = Streamable.of(1, 2, 3)
                .findFirst();
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1, result.get());
    }

    @Test
    void testFindFirstNoResult() {
        Optional<Integer> result = Streamable.<Integer>of()
                .findFirst();
        Assertions.assertTrue(result.isEmpty());
    }
}
