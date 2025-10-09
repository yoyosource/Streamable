package de.yoyosource.streamable;

import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class GenericTest {

    @Nested
    class Sequential {
        @Test
        void testGeneric() {
            long allCount = Streamable.of(1, 2, 3, 4)
                    .generic()
                    .collectEach(JavaStream::count)
                    .peek(count -> Assertions.assertEquals(count, 4))
                    .count();
            Assertions.assertEquals(1, allCount);
        }
    }

    @Nested
    class Parallel {
        @Test
        void testGeneric() {
            long allCount = Streamable.of(1, 2, 3, 4)
                    .parallel(3)
                    .generic()
                    .collectEach(JavaStream::count)
                    .peek(count -> Assertions.assertEquals(count, 4))
                    .count();
            Assertions.assertEquals(1, allCount);
        }
    }
}
