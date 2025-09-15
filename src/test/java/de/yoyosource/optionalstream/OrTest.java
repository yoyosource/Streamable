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

class OrTest {

    @Nested
    class Sequential {
        @Test
        void testOr() {
            List<Optional<Integer>> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .as(OptionalStream.OptionalStream())
                    .or(() -> Optional.of(4))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isPresent());
            Assertions.assertTrue(list.get(1).isPresent());
            Assertions.assertTrue(list.get(2).isPresent());
            Assertions.assertEquals(1, list.get(0).get());
            Assertions.assertEquals(4, list.get(1).get());
            Assertions.assertEquals(3, list.get(2).get());
        }

        @Test
        void testOrWithEmpty() {
            List<Optional<Integer>> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .as(OptionalStream.OptionalStream())
                    .or(Optional::empty)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isPresent());
            Assertions.assertTrue(list.get(1).isEmpty());
            Assertions.assertTrue(list.get(2).isPresent());
            Assertions.assertEquals(1, list.get(0).get());
            Assertions.assertEquals(3, list.get(2).get());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testOr() {
            List<Optional<Integer>> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .parallel(3)
                    .as(OptionalStream.OptionalStream())
                    .or(() -> Optional.of(4))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isPresent());
            Assertions.assertTrue(list.get(1).isPresent());
            Assertions.assertTrue(list.get(2).isPresent());
            Assertions.assertEquals(1, list.get(0).get());
            Assertions.assertEquals(4, list.get(1).get());
            Assertions.assertEquals(3, list.get(2).get());
        }

        @Test
        void testOrWithEmpty() {
            List<Optional<Integer>> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .parallel(3)
                    .as(OptionalStream.OptionalStream())
                    .or(Optional::empty)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isPresent());
            Assertions.assertTrue(list.get(1).isEmpty());
            Assertions.assertTrue(list.get(2).isPresent());
            Assertions.assertEquals(1, list.get(0).get());
            Assertions.assertEquals(3, list.get(2).get());
        }
    }
}
