package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class ScanTest {

    @Test
    void testScan() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .as(AdvancedStream.AdvancedStream())
                .scan(Integer::sum)
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0));
        Assertions.assertEquals(3, list.get(1));
        Assertions.assertEquals(6, list.get(2));
    }
}
