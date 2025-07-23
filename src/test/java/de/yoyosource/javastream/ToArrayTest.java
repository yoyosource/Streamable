package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ToArrayTest {

    @Test
    void testToArrayNoArgs() {
        Object[] array = Streamable.of(1, 2, 3)
                .toArray();
        Assertions.assertEquals(3, array.length);
        Assertions.assertInstanceOf(Integer.class, array[0]);
        Assertions.assertEquals(1, array[0]);
        Assertions.assertInstanceOf(Integer.class, array[1]);
        Assertions.assertEquals(2, array[1]);
        Assertions.assertInstanceOf(Integer.class, array[2]);
        Assertions.assertEquals(3, array[2]);
    }

    @Test
    void testToArrayObjectCreator() {
        Object[] array = Streamable.of(1, 2, 3)
                .toArray(Object[]::new);
        Assertions.assertEquals(3, array.length);
        Assertions.assertInstanceOf(Integer.class, array[0]);
        Assertions.assertEquals(1, array[0]);
        Assertions.assertInstanceOf(Integer.class, array[1]);
        Assertions.assertEquals(2, array[1]);
        Assertions.assertInstanceOf(Integer.class, array[2]);
        Assertions.assertEquals(3, array[2]);
    }

    @Test
    void testToArrayIntegerCreator() {
        Integer[] array = Streamable.of(1, 2, 3)
                .toArray(Integer[]::new);
        Assertions.assertEquals(3, array.length);
        Assertions.assertInstanceOf(Integer.class, array[0]);
        Assertions.assertEquals(1, array[0]);
        Assertions.assertInstanceOf(Integer.class, array[1]);
        Assertions.assertEquals(2, array[1]);
        Assertions.assertInstanceOf(Integer.class, array[2]);
        Assertions.assertEquals(3, array[2]);
    }
}
