package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AllMatchTest {

    @Test
    void testAllMatchMatches() {
        boolean result = Streamable.of(1, 2, 3)
                .allMatch(integer -> integer == 1);
        Assertions.assertFalse(result);
    }

    @Test
    void testAllMatchNoMatch() {
        boolean result = Streamable.of(1, 2, 3)
                .allMatch(integer -> integer != 4);
        Assertions.assertTrue(result);
    }
}
