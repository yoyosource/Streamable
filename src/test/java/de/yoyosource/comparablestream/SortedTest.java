package de.yoyosource.comparablestream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.ComparableStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class SortedTest {

    @Test
    void testSorted() {
        List<Integer> result = Streamable.of(3, 2, 1)
                .as(ComparableStream.ComparableStream())
                .sorted()
                .as(JavaStream.JavaStream())
                .toList();
        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals(1, result.get(0));
        Assertions.assertEquals(2, result.get(1));
        Assertions.assertEquals(3, result.get(2));
    }
}
