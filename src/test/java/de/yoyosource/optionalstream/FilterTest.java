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

class FilterTest {

    @Nested
    class Sequential {
        @Test
        void testFilter() {
            List<Optional<Integer>> list = Streamable.of(Optional.of(1), Optional.of(2), Optional.of(3))
                    .as(OptionalStream.OptionalStream())
                    .filter(integer -> integer < 3)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isPresent());
            Assertions.assertEquals(2, list.get(1).get());
            Assertions.assertTrue(list.get(1).isPresent());
            Assertions.assertEquals(1, list.get(0).get());
            Assertions.assertTrue(list.get(2).isEmpty());
        }

        @Test
        void testFilterNoResults() {
            List<Optional<Integer>> list = Streamable.of(Optional.of(1), Optional.of(2), Optional.of(3))
                    .as(OptionalStream.OptionalStream())
                    .filter(integer -> integer < 1)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isEmpty());
            Assertions.assertTrue(list.get(1).isEmpty());
            Assertions.assertTrue(list.get(2).isEmpty());
        }

        @Test
        void testFilterWithEmpty() {
            List<Optional<Integer>> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .as(OptionalStream.OptionalStream())
                    .filter(integer -> integer < 3)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isPresent());
            Assertions.assertEquals(1, list.get(0).get());
            Assertions.assertTrue(list.get(1).isEmpty());
            Assertions.assertTrue(list.get(2).isEmpty());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testFilter() {
            List<Optional<Integer>> list = Streamable.of(Optional.of(1), Optional.of(2), Optional.of(3))
                    .parallel(3)
                    .as(OptionalStream.OptionalStream())
                    .filter(integer -> integer < 3)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isPresent());
            Assertions.assertEquals(2, list.get(1).get());
            Assertions.assertTrue(list.get(1).isPresent());
            Assertions.assertEquals(1, list.get(0).get());
            Assertions.assertTrue(list.get(2).isEmpty());
        }

        @Test
        void testFilterNoResults() {
            List<Optional<Integer>> list = Streamable.of(Optional.of(1), Optional.of(2), Optional.of(3))
                    .parallel(3)
                    .as(OptionalStream.OptionalStream())
                    .filter(integer -> integer < 1)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isEmpty());
            Assertions.assertTrue(list.get(1).isEmpty());
            Assertions.assertTrue(list.get(2).isEmpty());
        }

        @Test
        void testFilterWithEmpty() {
            List<Optional<Integer>> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .parallel(3)
                    .as(OptionalStream.OptionalStream())
                    .filter(integer -> integer < 3)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).isPresent());
            Assertions.assertEquals(1, list.get(0).get());
            Assertions.assertTrue(list.get(1).isEmpty());
            Assertions.assertTrue(list.get(2).isEmpty());
        }
    }
}
