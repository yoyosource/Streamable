package de.yoyosource.optionalstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.OptionalStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class IsPresentTest {

    @Test
    void testIsPresent() {
        List<Optional<Integer>> list = Streamable.<Optional<Integer>>of(Optional.of(1), Optional.empty(), Optional.of(3))
                .as(OptionalStream.OptionalStream())
                .isPresent()
                .as(JavaStream.JavaStream())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(1, list.get(0).get());
        Assertions.assertEquals(3, list.get(1).get());
    }
}
