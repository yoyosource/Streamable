package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class CollectTest {

    @Test
    void testCollectToList() {
        List<Integer> list = Streamable.of(1, 2, 3)
                .collect(Collectors.toList());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0));
        Assertions.assertEquals(2, list.get(1));
        Assertions.assertEquals(3, list.get(2));
    }

    @Test
    void testCollectToSet() {
        Set<Integer> set = Streamable.of(1, 2, 3)
                .collect(Collectors.toSet());
        Assertions.assertEquals(3, set.size());
        Assertions.assertTrue(set.contains(1));
        Assertions.assertTrue(set.contains(2));
        Assertions.assertTrue(set.contains(3));
    }

    @Test
    void testCollectJoining() {
        String text = Streamable.of("1", "2", "3")
                .collect(Collectors.joining());
        Assertions.assertEquals("123", text);
    }

    @Test
    void testCollectJoiningWithDelimiter() {
        String text = Streamable.of("1", "2", "3")
                .collect(Collectors.joining(", "));
        Assertions.assertEquals("1, 2, 3", text);
    }

    @Test
    void testCollectJoiningWithPrefix() {
        String text = Streamable.of("1", "2", "3")
                .collect(Collectors.joining("", "[", ""));
        Assertions.assertEquals("[123", text);
    }

    @Test
    void testCollectJoiningWithSuffix() {
        String text = Streamable.of("1", "2", "3")
                .collect(Collectors.joining("", "", "]"));
        Assertions.assertEquals("123]", text);
    }
}
