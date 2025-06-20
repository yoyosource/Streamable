package de.yoyosource.numberstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.NumberStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class SortedTest {

    @Test
    void testModus() {
        List<Integer> result = Streamable.of(3, 2, 1)
                .as(NumberStream.NumberStream())
                .sorted()
                .as(JavaStream.JavaStream())
                .toList();
        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals(1, result.get(0));
        Assertions.assertEquals(2, result.get(1));
        Assertions.assertEquals(3, result.get(2));
    }

    @Test
    void testModusNoResult() {
        List<Integer> result = Streamable.<Integer>of()
                .as(NumberStream.NumberStream())
                .sorted()
                .as(JavaStream.JavaStream())
                .toList();
        Assertions.assertEquals(0, result.size());
    }
}
