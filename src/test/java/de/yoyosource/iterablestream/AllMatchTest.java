package de.yoyosource.iterablestream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.IterableStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class AllMatchTest {

    @Test
    void testAllMatch() {
        List<Boolean> list = Streamable.of(List.of(1, 2, 3), Streamable.of(4, 5, 6))
                .as(IterableStream.IterableStream())
                .allMatch(integer -> integer > 3)
                .collect(Collectors.toList());
        Assertions.assertEquals(2, list.size());
        Assertions.assertFalse(list.get(0));
        Assertions.assertTrue(list.get(1));
    }
}
