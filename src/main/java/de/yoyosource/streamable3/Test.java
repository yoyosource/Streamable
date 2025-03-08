package de.yoyosource.streamable3;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        List<Long> times = new ArrayList<>();

        for (int counter = 0; counter < 100; counter++) {
            Sequence<Long> sequence = new Sequence<>();

            for (int i = 0; i < 100; i++) {
                Sequence.Inserter<Long> inserter = sequence.inserter();
                int finalI = i;
                new Thread(() -> {
                    for (int j = 0; j < 1_000_000; j++) {
                        inserter.add(finalI * (long) j);
                    }
                    inserter.release();
                }).start();
            }

            System.out.println("Starting: " + counter);
            long time = System.currentTimeMillis();
            while (sequence.hasUnreleasedInserter()) {
                for (long s : sequence) {
                    // System.out.println(counter + ": " + sequence.getIndex() + "_" + s);
                }
            }
            time = System.currentTimeMillis() - time;
            times.add(time);
            System.out.println("Ending  : " + counter + "   " + time + "ms");

            /*
            long total = 0;

            long time = System.currentTimeMillis();
            total = Stream.iterate(0L, i -> i < 100_000_000, i -> i + 1)
                    .reduce(0L, Long::sum);
            for (long i = 0; i < 100_000_000; i++) {
                total += i;
            }
            time = System.currentTimeMillis() - time;
            times.add(time);
            System.out.println(total);
             */
        }
        times.sort(Long::compareTo);

        System.out.println("");
        System.out.println("Median: " + times.get(times.size() / 2) + "ms");
        System.out.println("Average: " + times.stream().mapToLong(Long::longValue).average().orElseThrow() + "ms");
        System.out.println("Min: " + times.stream().min(Long::compareTo).orElseThrow() + "ms");
        System.out.println("Max: " + times.stream().max(Long::compareTo).orElseThrow() + "ms");
        System.out.println("Total: " + times.stream().mapToLong(Long::longValue).sum() + "ms");
    }
}
