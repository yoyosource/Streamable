package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ConsecutiveElementCountTest {

    @Test
    void testConsecutiveElementCount() {
        List<Map.Entry<Integer, Long>> list = Streamable.of(1, 2, 2, 3, 3, 3)
                .as(AdvancedStream.AdvancedStream())
                .consecutiveElementCount()
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0).getKey());
        Assertions.assertEquals(1, list.get(0).getValue());
        Assertions.assertEquals(2, list.get(1).getKey());
        Assertions.assertEquals(2, list.get(1).getValue());
        Assertions.assertEquals(3, list.get(2).getKey());
        Assertions.assertEquals(3, list.get(2).getValue());
    }
}
