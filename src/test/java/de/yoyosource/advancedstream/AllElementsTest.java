package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class AllElementsTest {

    @Test
    void allElementsTest() {
        List<List<Integer>> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .allElements()
                .as(JavaStream.JavaStream())
                .toList();
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(3, list.get(0).size());
        Assertions.assertEquals(1, list.get(0).get(0));
        Assertions.assertEquals(2, list.get(0).get(1));
        Assertions.assertEquals(3, list.get(0).get(2));
    }

    @Test
    void allElementsTestNoElements() {
        List<List<Integer>> list = Streamable.<Integer>of()
                .as(AdvancedStream.AdvancedStream())
                .allElements()
                .as(JavaStream.JavaStream())
                .toList();
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(0, list.get(0).size());
    }
}
