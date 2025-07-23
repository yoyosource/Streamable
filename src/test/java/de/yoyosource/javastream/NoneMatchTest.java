package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoneMatchTest {

    @Test
    void testNoneMatchMatches() {
        boolean result = Streamable.of(1, 2, 3)
                .noneMatch(integer -> integer == 1);
        Assertions.assertFalse(result);
    }

    @Test
    void testNoneMatchNoMatch() {
        boolean result = Streamable.of(1, 2, 3)
                .noneMatch(integer -> integer == 4);
        Assertions.assertTrue(result);
    }
}
