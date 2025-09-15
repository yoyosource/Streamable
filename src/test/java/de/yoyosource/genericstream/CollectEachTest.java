package de.yoyosource.genericstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.GenericStream;
import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class CollectEachTest {

    @Nested
    class Sequential {
        @Test
        void testCollectEach() {
            List<Long> list = Streamable.of(List.of(1, 2, 3), Streamable.of(4, 5, 6))
                    .map(Streamable::from)
                    .as(GenericStream.GenericStream())
                    .collectEach(JavaStream::count)
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(3, list.get(0));
            Assertions.assertEquals(3, list.get(1));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testCollectEach() {
            List<Long> list = Streamable.of(List.of(1, 2, 3), Streamable.of(4, 5, 6))
                    .parallel(3)
                    .map(Streamable::from)
                    .as(GenericStream.GenericStream())
                    .collectEach(JavaStream::count)
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(3, list.get(0));
            Assertions.assertEquals(3, list.get(1));
        }
    }
}
