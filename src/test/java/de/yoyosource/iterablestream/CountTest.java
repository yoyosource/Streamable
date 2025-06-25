package de.yoyosource.iterablestream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.IterableStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class CountTest {

    @Test
    void testCollectEach() {
        Long result = Streamable.of(List.of(1, 2, 3), Streamable.of(4, 5, 6))
                .as(IterableStream.IterableStream())
                .count()
                .sum()
                .orElseThrow();
        Assertions.assertEquals(6, result);
    }
}
