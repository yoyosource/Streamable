package de.yoyosource.optionalstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.Try;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.OptionalStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

class GetTest {

    @Nested
    class Sequential {
        @Test
        void testGetOnOptional() {
            List<Try<Integer, NoSuchElementException>> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .as(OptionalStream.OptionalStream())
                    .get()
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).successful());
            Assertions.assertEquals(1, list.get(0).getSuccess());
            Assertions.assertTrue(list.get(1).failed());
            Assertions.assertInstanceOf(NoSuchElementException.class, list.get(1).getFailure());
            Assertions.assertTrue(list.get(2).successful());
            Assertions.assertEquals(3, list.get(2).getSuccess());
        }

        @Test
        void testGetOnPresent() {
            List<Integer> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .as(OptionalStream.OptionalStream())
                    .isPresent()
                    .get()
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(3, list.get(1));
        }
    }

    @Nested
    class Parallel {
        @Test
        void testGetOnOptional() {
            List<Try<Integer, NoSuchElementException>> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .parallel(3)
                    .as(OptionalStream.OptionalStream())
                    .get()
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.get(0).successful());
            Assertions.assertEquals(1, list.get(0).getSuccess());
            Assertions.assertTrue(list.get(1).failed());
            Assertions.assertInstanceOf(NoSuchElementException.class, list.get(1).getFailure());
            Assertions.assertTrue(list.get(2).successful());
            Assertions.assertEquals(3, list.get(2).getSuccess());
        }

        @Test
        void testGetOnPresent() {
            List<Integer> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                    .parallel(3)
                    .as(OptionalStream.OptionalStream())
                    .isPresent()
                    .get()
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(3, list.get(1));
        }
    }
}
