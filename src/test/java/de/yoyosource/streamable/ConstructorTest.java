package de.yoyosource.streamable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

class ConstructorTest {

    @Nested
    class Sequential {
        @Test
        void emptyTest() {
            List list = Streamable.empty()
                    .toList();
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void ofTest() {
            List<Integer> list = Streamable.of(1)
                    .toList();
            Assertions.assertEquals(1, list.size());
            Assertions.assertEquals(1, list.get(0));
        }

        @Test
        void ofTestWithNull() {
            List list = Streamable.of((Integer) null)
                    .toList();
            Assertions.assertEquals(1, list.size());
            Assertions.assertNull(list.get(0));
        }

        @Test
        void ofNullableTest() {
            List<Integer> list = Streamable.ofNullable(1)
                    .toList();
            Assertions.assertEquals(1, list.size());
            Assertions.assertEquals(1, list.get(0));
        }

        @Test
        void ofNullableTestWithNull() {
            List list = Streamable.ofNullable(null)
                    .toList();
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void ofTestVarArgs() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void ofTestVarArgsEmpty() {
            List list = Streamable.of()
                    .toList();
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void iterateTest() {
            List<Integer> list = Streamable.iterate(0, i -> i + 1)
                    .limit(5)
                    .toList();
            Assertions.assertEquals(5, list.size());
            Assertions.assertEquals(0, list.get(0));
            Assertions.assertEquals(1, list.get(1));
            Assertions.assertEquals(2, list.get(2));
            Assertions.assertEquals(3, list.get(3));
            Assertions.assertEquals(4, list.get(4));
        }

        @Test
        void iterateTestWithLimit() {
            List<Integer> list = Streamable.iterate(0, i -> i < 5, i -> i + 1)
                    .toList();
            Assertions.assertEquals(5, list.size());
            Assertions.assertEquals(0, list.get(0));
            Assertions.assertEquals(1, list.get(1));
            Assertions.assertEquals(2, list.get(2));
            Assertions.assertEquals(3, list.get(3));
            Assertions.assertEquals(4, list.get(4));
        }

        @Test
        void generateTest() {
            List<Integer> list = Streamable.generate(() -> 0)
                    .limit(5)
                    .toList();
            Assertions.assertEquals(5, list.size());
            Assertions.assertEquals(0, list.get(0));
            Assertions.assertEquals(0, list.get(1));
            Assertions.assertEquals(0, list.get(2));
            Assertions.assertEquals(0, list.get(3));
            Assertions.assertEquals(0, list.get(4));
        }

        @Test
        void fromTestStream() {
            List<Integer> list = Streamable.from(Stream.of(1, 2, 3))
                    .toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void fromTestIterableWithStreamable() {
            List<Integer> list = Streamable.from(Streamable.of(1, 2, 3))
                    .toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void fromTestIterableWithList() {
            List<Integer> list = Streamable.from(List.of(1, 2, 3))
                    .toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void fromTestIterableWithSet() {
            List<Integer> list = Streamable.from(Set.of(1, 2, 3))
                    .toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.contains(1));
            Assertions.assertTrue(list.contains(2));
            Assertions.assertTrue(list.contains(3));
        }

        @Test
        void fromTestIterator() {
            List<Integer> list = Streamable.from(new Iterator<Integer>() {
                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Integer next() {
                    return 0;
                }
            }).limit(3).toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(0, list.get(0));
            Assertions.assertEquals(0, list.get(1));
            Assertions.assertEquals(0, list.get(2));
        }
    }

    @Nested
    class Parallel {
        @Test
        void emptyTest() {
            List list = Streamable.empty()
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void ofTest() {
            List<Integer> list = Streamable.of(1)
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(1, list.size());
            Assertions.assertEquals(1, list.get(0));
        }

        @Test
        void ofTestWithNull() {
            List list = Streamable.of((Integer) null)
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(1, list.size());
            Assertions.assertNull(list.get(0));
        }

        @Test
        void ofNullableTest() {
            List<Integer> list = Streamable.ofNullable(1)
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(1, list.size());
            Assertions.assertEquals(1, list.get(0));
        }

        @Test
        void ofNullableTestWithNull() {
            List list = Streamable.ofNullable(null)
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void ofTestVarArgs() {
            List<Integer> list = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void ofTestVarArgsEmpty() {
            List list = Streamable.of()
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(0, list.size());
        }

        @Test
        void iterateTest() {
            List<Integer> list = Streamable.iterate(0, i -> i + 1)
                    .parallel(3)
                    .limit(5)
                    .toList();
            Assertions.assertEquals(5, list.size());
            Assertions.assertEquals(0, list.get(0));
            Assertions.assertEquals(1, list.get(1));
            Assertions.assertEquals(2, list.get(2));
            Assertions.assertEquals(3, list.get(3));
            Assertions.assertEquals(4, list.get(4));
        }

        @Test
        void iterateTestWithLimit() {
            List<Integer> list = Streamable.iterate(0, i -> i < 5, i -> i + 1)
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(5, list.size());
            Assertions.assertEquals(0, list.get(0));
            Assertions.assertEquals(1, list.get(1));
            Assertions.assertEquals(2, list.get(2));
            Assertions.assertEquals(3, list.get(3));
            Assertions.assertEquals(4, list.get(4));
        }

        @Test
        void generateTest() {
            List<Integer> list = Streamable.generate(() -> 0)
                    .parallel(3)
                    .limit(5)
                    .toList();
            Assertions.assertEquals(5, list.size());
            Assertions.assertEquals(0, list.get(0));
            Assertions.assertEquals(0, list.get(1));
            Assertions.assertEquals(0, list.get(2));
            Assertions.assertEquals(0, list.get(3));
            Assertions.assertEquals(0, list.get(4));
        }

        @Test
        void fromTestStream() {
            List<Integer> list = Streamable.from(Stream.of(1, 2, 3))
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void fromTestIterableWithStreamable() {
            List<Integer> list = Streamable.from(Streamable.of(1, 2, 3))
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void fromTestIterableWithList() {
            List<Integer> list = Streamable.from(List.of(1, 2, 3))
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(1, list.get(0));
            Assertions.assertEquals(2, list.get(1));
            Assertions.assertEquals(3, list.get(2));
        }

        @Test
        void fromTestIterableWithSet() {
            List<Integer> list = Streamable.from(Set.of(1, 2, 3))
                    .parallel(3)
                    .toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertTrue(list.contains(1));
            Assertions.assertTrue(list.contains(2));
            Assertions.assertTrue(list.contains(3));
        }

        @Test
        void fromTestIterator() {
            List<Integer> list = Streamable.from(new Iterator<Integer>() {
                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Integer next() {
                    return 0;
                }
            }).parallel(3).limit(3).toList();
            Assertions.assertEquals(3, list.size());
            Assertions.assertEquals(0, list.get(0));
            Assertions.assertEquals(0, list.get(1));
            Assertions.assertEquals(0, list.get(2));
        }
    }
}
