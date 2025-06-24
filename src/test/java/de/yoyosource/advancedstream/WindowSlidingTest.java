package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class WindowSlidingTest {

    @Test
    void testWindowSlidingSize1() {
        List<List<Integer>> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .windowSliding(1)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0).size());
        Assertions.assertEquals(1, list.get(0).get(0));
        Assertions.assertEquals(1, list.get(1).size());
        Assertions.assertEquals(2, list.get(1).get(0));
        Assertions.assertEquals(1, list.get(2).size());
        Assertions.assertEquals(3, list.get(2).get(0));
    }

    @Test
    void testWindowSlidingSize1Partial() {
        List<List<Integer>> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .windowSliding(1, true)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0).size());
        Assertions.assertEquals(1, list.get(0).get(0));
        Assertions.assertEquals(1, list.get(1).size());
        Assertions.assertEquals(2, list.get(1).get(0));
        Assertions.assertEquals(1, list.get(2).size());
        Assertions.assertEquals(3, list.get(2).get(0));
    }

    @Test
    void testWindowSlidingSize2() {
        List<List<Integer>> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .windowSliding(2)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(2, list.get(0).size());
        Assertions.assertEquals(1, list.get(0).get(0));
        Assertions.assertEquals(2, list.get(0).get(1));
        Assertions.assertEquals(2, list.get(1).size());
        Assertions.assertEquals(2, list.get(1).get(0));
        Assertions.assertEquals(3, list.get(1).get(1));
    }

    @Test
    void testWindowSlidingSize2Partial() {
        List<List<Integer>> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .windowSliding(2, true)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(2, list.get(0).size());
        Assertions.assertEquals(1, list.get(0).get(0));
        Assertions.assertEquals(2, list.get(0).get(1));
        Assertions.assertEquals(2, list.get(1).size());
        Assertions.assertEquals(2, list.get(1).get(0));
        Assertions.assertEquals(3, list.get(1).get(1));
    }

    @Test
    void testWindowSlidingSize3() {
        List<List<Integer>> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .windowSliding(3)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(3, list.get(0).size());
        Assertions.assertEquals(1, list.get(0).get(0));
        Assertions.assertEquals(2, list.get(0).get(1));
        Assertions.assertEquals(3, list.get(0).get(2));
    }

    @Test
    void testWindowSlidingSize3Partial() {
        List<List<Integer>> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .windowSliding(3, true)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(3, list.get(0).size());
        Assertions.assertEquals(1, list.get(0).get(0));
        Assertions.assertEquals(2, list.get(0).get(1));
        Assertions.assertEquals(3, list.get(0).get(2));
    }

    @Test
    void testWindowSlidingSize4() {
        List<List<Integer>> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .windowSliding(4)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(0, list.size());
    }

    @Test
    void testWindowSlidingSize4Partial() {
        List<List<Integer>> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .windowSliding(4, true)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(3, list.get(0).size());
        Assertions.assertEquals(1, list.get(0).get(0));
        Assertions.assertEquals(2, list.get(0).get(1));
        Assertions.assertEquals(3, list.get(0).get(2));
    }
}
