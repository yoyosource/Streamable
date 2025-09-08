package de.yoyosource.advancedstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;
import de.yoyosource.streamable.streams.ZippedStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class ZipTest {

    @Nested
    class Sequential {
        @Test
        void testZipSameSizeNonIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
            Assertions.assertEquals(3, list.get(2).a);
            Assertions.assertEquals(6, list.get(2).b);
        }

        @Test
        void testZipFirstSmallerNonIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
            Assertions.assertNull(list.get(2).a);
            Assertions.assertEquals(6, list.get(2).b);
        }

        @Test
        void testZipSecondSmallerNonIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
            Assertions.assertEquals(3, list.get(2).a);
            Assertions.assertNull(list.get(2).b);
        }

        @Test
        void testZipSameSizeIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6), true)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
            Assertions.assertEquals(3, list.get(2).a);
            Assertions.assertEquals(6, list.get(2).b);
        }

        @Test
        void testZipFirstSmallerIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6), true)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
        }

        @Test
        void testZipSecondSmallerIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5), true)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
        }
    }

    @Nested
    class Parallel {
        @Test
        void testZipSameSizeNonIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
            Assertions.assertEquals(3, list.get(2).a);
            Assertions.assertEquals(6, list.get(2).b);
        }

        @Test
        void testZipFirstSmallerNonIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
            Assertions.assertNull(list.get(2).a);
            Assertions.assertEquals(6, list.get(2).b);
        }

        @Test
        void testZipSecondSmallerNonIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5))
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
            Assertions.assertEquals(3, list.get(2).a);
            Assertions.assertNull(list.get(2).b);
        }

        @Test
        void testZipSameSizeIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6), true)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
            Assertions.assertEquals(3, list.get(2).a);
            Assertions.assertEquals(6, list.get(2).b);
        }

        @Test
        void testZipFirstSmallerIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5, 6), true)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
        }

        @Test
        void testZipSecondSmallerIgnoreNulls() {
            List<ZippedStream.Zip<Integer, Integer>> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(AdvancedStream.AdvancedStream())
                    .zip(Streamable.of(4, 5), true)
                    .as(JavaStream.JavaStream())
                    .collect(Collectors.toList());
            Assertions.assertEquals(2, list.size());
            Assertions.assertEquals(1, list.get(0).a);
            Assertions.assertEquals(4, list.get(0).b);
            Assertions.assertEquals(2, list.get(1).a);
            Assertions.assertEquals(5, list.get(1).b);
        }
    }
}
