package de.yoyosource.streamable;

import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static de.yoyosource.streamable.streams.JavaStream.JavaStream;
import static de.yoyosource.streamable.streams.NumberStream.NumberStream;
import static de.yoyosource.streamable.streams.NumberStream.SummaryStatistics;

public class Test {

    public static void main(String[] args) {
        test_1();
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
}
