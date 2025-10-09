package de.yoyosource.streamable;

import de.yoyosource.streamable.streams.JavaStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class AddTest {

    @Nested
    class Sequential {
        @Test
        void testAddElement() {
            long count = Streamable.of(1, 2, 3, 4)
                    .add(5)
                    .count();
            Assertions.assertEquals(5, count);
        }

        @Test
        void testAddMultipleElements() {
            long count = Streamable.of(1, 2, 3, 4)
                    .add(5, 6, 7, 8)
                    .count();
            Assertions.assertEquals(8, count);
        }

        @Test
        void testAddOfStream() {
            long allCount = Streamable.of(1, 2, 3, 4)
                    .generic()
                    .add(Streamable.of(5, 6, 7, 8, 9, 10))
                    .collectEach(JavaStream::count)
                    .count();
            Assertions.assertEquals(2, allCount);
        }
    }

    @Nested
    class Parallel {
        @Test
        void testAddElement() {
            long count = Streamable.of(1, 2, 3, 4)
                    .parallel(3)
                    .add(5)
                    .count();
            Assertions.assertEquals(5, count);
        }

        @Test
        void testAddMultipleElements() {
            long count = Streamable.of(1, 2, 3, 4)
                    .parallel(3)
                    .add(5, 6, 7, 8)
                    .count();
            Assertions.assertEquals(8, count);
        }

        @Test
        void testAddOfStream() {
            long allCount = Streamable.of(1, 2, 3, 4)
                    .parallel(3)
                    .generic()
                    .add(Streamable.of(5, 6, 7, 8, 9, 10))
                    .collectEach(JavaStream::count)
                    .count();
            Assertions.assertEquals(2, allCount);
        }
    }
}
