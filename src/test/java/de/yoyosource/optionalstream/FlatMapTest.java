package de.yoyosource.optionalstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.OptionalStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class FlatMapTest {

    @Nested
    class Sequential {
        @Test
        void testFlatMap() {
            List<Optional<Integer>> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .as(OptionalStream.OptionalStream())
                    .flatMap(integer -> Optional.of(integer * 2))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isPresent());
            Assertions.assertTrue(list.get(1).isEmpty());
            Assertions.assertTrue(list.get(2).isPresent());
            Assertions.assertEquals(2, list.get(0).get());
            Assertions.assertEquals(6, list.get(2).get());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testFlatMap() {
            List<Optional<Integer>> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .parallel(3)
                    .as(OptionalStream.OptionalStream())
                    .flatMap(integer -> Optional.of(integer * 2))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isPresent());
            Assertions.assertTrue(list.get(1).isEmpty());
            Assertions.assertTrue(list.get(2).isPresent());
            Assertions.assertEquals(2, list.get(0).get());
            Assertions.assertEquals(6, list.get(2).get());
        }
    }
}
