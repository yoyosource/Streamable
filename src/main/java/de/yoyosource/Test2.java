package de.yoyosource;

import de.yoyosource.streamable.Streamable;

import java.math.BigInteger;

public class Test2 {

    public static void main(String[] args) {
        benchmark(() -> {
            Streamable.iterate(BigInteger.ONE, bigInteger -> bigInteger.add(BigInteger.ONE))
                    .limit(100_000)
                    .parallel(100)
                    .reduce(BigInteger::multiply)
                    .orElse(null);
        });
    }

    private static void benchmark(Runnable runnable) {
        for (int i = 0; i < 1000; i++) {
            long start = System.nanoTime();
            runnable.run();
            long end = System.nanoTime();
            long time = (end - start) / 1_000;
            System.out.println(time + "µs");
        }

        long start = System.nanoTime();
        runnable.run();
        long end = System.nanoTime();
    }
}
