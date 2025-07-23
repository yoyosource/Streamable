package de.yoyosource.zippedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.ZippedStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class FilterRightTest {

    @Test
    void testFilterRight() {
        List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .zip(Streamable.of(4, 5, 6))
                .filterRight(integer -> integer < 6)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(1, list.get(0).a);
        Assertions.assertEquals(4, list.get(0).b);
        Assertions.assertEquals(2, list.get(1).a);
        Assertions.assertEquals(5, list.get(1).b);
    }

    @Test
    void testFilterRightNoResult() {
        List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .zip(Streamable.of(4, 5, 6))
                .filterRight(integer -> integer < 4)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(0, list.size());
    }
}
