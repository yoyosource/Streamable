package de.yoyosource.zippedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.ZippedStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class FilterLeftTest {

    @Nested
    class Sequential {
        @Test
        void testFilterLeft() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .filterLeft(integer -> integer < 3)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
        }

        @Test
        void testFilterLeftNoResult() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .filterLeft(integer -> integer < 1)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }
    }

    @Nested
    class Parallel {
        @Test
        @Disabled
        void testFilterLeft() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .filterLeft(integer -> integer < 3)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
        }

        @Test
        void testFilterLeftNoResult() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .filterLeft(integer -> integer < 1)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(0, list.size());
        }
    }
}
