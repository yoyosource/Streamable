package de.yoyosource.javastream;

import de.yoyosource.streamable.Streamable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class ReduceTest {

    @Test
    void testReduceWithIdentityNoElements() {
        int sum = Streamable.<Integer>of()
                .reduce(1, Integer::sum);
        Assertions.assertEquals(1, sum);
    }

    @Test
    void testReduceWithIdentity() {
        int sum = Streamable.of(1, 2, 3)
                .reduce(1, Integer::sum);
        Assertions.assertEquals(7, sum);
    }

    @Test
    void testReduceNoIdentityNoElements() {
        Optional<Integer> sum = Streamable.<Integer>of()
                .reduce(Integer::sum);
        Assertions.assertTrue(sum.isEmpty());
    }

    @Test
    void testReduceNoIdentity() {
        Optional<Integer> sum = Streamable.of(1, 2, 3)
                .reduce(Integer::sum);
        Assertions.assertTrue(sum.isPresent());
        Assertions.assertEquals(6, sum.get());
    }
}
