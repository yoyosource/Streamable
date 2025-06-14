package de.yoyosource;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class Test2 {

    public static void main(String[] args) throws Exception {
        List<BigInteger> result = Streamable.iterate(BigInteger.ONE, bigInteger -> bigInteger.add(BigInteger.ONE)).limit(1_000)
                .as(AdvancedStream.AdvancedStream())
                .scan(BigInteger::add)
                .as(JavaStream.JavaStream())
                // .peek(bigInteger -> System.out.println(": " + bigInteger))
                .collect(Collectors.toList());
        System.out.println(result);

        testFactorialSequential();
        testFactorialParallel();
    }

    public static void testFactorialSequential() {
        BigInteger result = Streamable.iterate(BigInteger.ONE, bigInteger -> bigInteger.add(BigInteger.ONE))
                .limit(1_000)
                .reduce(BigInteger::multiply)
                .orElse(null);
        System.out.println(result);
    }

    public static void testFactorialParallel() {
        BigInteger result = Streamable.iterate(BigInteger.ONE, bigInteger -> bigInteger.add(BigInteger.ONE))
                .limit(1_000)
                .parallel(2)
                .reduce(BigInteger::multiply)
                .orElse(null);
        System.out.println(result);
    }
}
