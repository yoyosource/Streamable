package de.yoyosource.streamable;

import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.IterableStream;
import de.yoyosource.streamable.streams.JavaStream;

import java.util.*;
import java.util.stream.Stream;

import static de.yoyosource.streamable.streams.JavaStream.JavaStream;
import static de.yoyosource.streamable.streams.NumberStream.NumberStream;
import static de.yoyosource.streamable.streams.NumberStream.SummaryStatistics;

public class Test {

    public static void main(String[] args) {
        if (false) test_3();
        if (false) test_4();
        if (false) test_5();
        if (false) test_6();
        if (true) test_7();
    }

    private static void test_1() {
        Random random = new Random();
        Streamable.generate(random::nextBoolean)
                // .parallel(100)
                // .limit(1_000_000_000)
                .limit(1_000_000)
                .as(AdvancedStream.AdvancedStream())
                .consecutiveElementCount()
                .as(JavaStream())
                .max(Map.Entry.comparingByValue())
                .ifPresent(System.out::println);
    }

    private static void test_2() {
        Random random = new Random();

        long time = System.currentTimeMillis();

        long maxConsecutiveElements = 0;
        boolean maxConsecutiveElement = false;

        long consecutiveElements = 0;
        boolean currentElements = false;
        for (long i = 0; i < 1_000_000_000; i++) {
            boolean element = random.nextBoolean();
            if (consecutiveElements == 0 || currentElements == element) {
                currentElements = element;
                consecutiveElements++;
            } else {
                if (consecutiveElements > maxConsecutiveElements) {
                    maxConsecutiveElements = consecutiveElements;
                    maxConsecutiveElement = element;
                }
                currentElements = element;
                consecutiveElements = 1;
            }
        }

        time = System.currentTimeMillis() - time;
        System.out.println(maxConsecutiveElements + ": " + maxConsecutiveElement + " in " + time + "ms");
    }

    private static void test_3() {
        long count = Streamable.iterate(0L, aLong -> aLong < 10_000_000L, aLong -> aLong + 1)
                .parallel(16)
                .flatMap(aLong -> List.of(aLong, aLong, aLong, aLong, aLong))
                .count();
        System.out.println(count);
    }

    private static void test_4() {
        Streamable.of(1, 2, 3, 4)
                .map(integer -> List.of(integer, integer, integer, integer))
                .as(IterableStream.IterableStream())
                .map(integer -> integer * 2)
                .distinct()
                .flatten()
                .forEach(System.out::println);
    }

    private static void test_5() {
        Streamable.of(1, 2, 3, 4)
                .iterator()
                .forEachRemaining(System.out::println);
    }

    private static void test_6() {
        Streamable.generate(() -> new Random().nextBoolean())
                .flatMap(aBoolean -> List.of(aBoolean, aBoolean, aBoolean, aBoolean, aBoolean))
                .flatMap(aBoolean -> List.of(aBoolean, aBoolean, aBoolean, aBoolean, aBoolean))
                .flatMap(aBoolean -> List.of(aBoolean, aBoolean, aBoolean, aBoolean, aBoolean))
                .flatMap(aBoolean -> List.of(aBoolean, aBoolean, aBoolean, aBoolean, aBoolean))
                .flatMap(aBoolean -> List.of(aBoolean, aBoolean, aBoolean, aBoolean, aBoolean))
                .flatMap(aBoolean -> List.of(aBoolean, aBoolean, aBoolean, aBoolean, aBoolean))
                // .limit(1_000_000_000_000L)
                .limit(100_000_000L)
                .count();
    }

    private static void test_7() {
        Streamable.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .as(AdvancedStream.AdvancedStream())
                .zip(Streamable.of(4, 3, 2, 1, 0), true)
                .forEach(System.out::println);
    }
}
