package de.yoyosource.zippedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class GetRightTest {

    @Nested
    class Sequential {
        @Test
        void testGetRight() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .getRight()
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(4, list.get(0));
            Assertions.assertEquals(5, list.get(1));
            Assertions.assertEquals(6, list.get(2));
        }

        @Test
        void testGetRightNull() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.<Integer>of())
                    .getRight()
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertNull(list.get(0));
            Assertions.assertNull(list.get(1));
            Assertions.assertNull(list.get(2));
        }
    }

    @Nested
    class Parallel {
        @Test
        @Disabled
        void testGetRight() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .getRight()
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(4, list.get(0));
            Assertions.assertEquals(5, list.get(1));
            Assertions.assertEquals(6, list.get(2));
        }

        @Test
        @Disabled
        void testGetRightNull() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.<Integer>of())
                    .getRight()
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertNull(list.get(0));
            Assertions.assertNull(list.get(1));
            Assertions.assertNull(list.get(2));
        }
    }
}
