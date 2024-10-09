package de.yoyosource;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.impl.AdvancedStream;
import de.yoyosource.streamable.impl.JavaStream;
import de.yoyosource.streamable.impl.NumberStream;
import de.yoyosource.streamable.impl.TryingStream;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Test {

    public static void main(String[] args) {
        testSimpleForEach();
        testGroupBy();
        testIteratorAfterFlatMap();
        testCount();
        testMapMulti();
        testTry();
        testFindFirst();

        if (true) return;

        Streamable.from(Stream.of(1, 2, 3))
                .as(JavaStream.type())
                .flatMap(integer -> Streamable.from(Stream.of(integer, integer + 1)))
                .as(JavaStream.type())
                .skip(0)
                .limit(4)
                // .map(integer -> BigDecimal.valueOf(integer))
                .as(NumberStream.type())
                .sum()
                .ifPresent(System.out::println);
                // .as(GroupingStream.type())
                // .distinctBy(integer -> integer % 2)
                // .groupBy(integer -> integer % 2)
                // .as(ForEachStream.type())
                // .forEach(integerListMap -> System.out.println(integerListMap));

        System.out.println();
        for (int i : Streamable.from(Stream.of(0, 1, 2))) {
            System.out.println(i);
        }

        System.out.println();
        long count = Streamable.from(Stream.of(1))
                .as(JavaStream.type())
                .flatMap(d -> Streamable.from(Stream.generate(Math::random)))
                .limit(10)
                .count();
        System.out.println(count);

        Streamable.from(Stream.of(1))
                .as(JavaStream.type())
                .flatMap(d -> Streamable.from(Stream.generate(Math::random)))
                .limit(10)
                .iterator()
                .forEachRemaining(System.out::println);
    }

    private static void testSimpleForEach() {
        Streamable.from(Stream.of(1, 2, 3))
                .forEach(System.out::println);
    }

    private static void testGroupBy() {
        Streamable.from(Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                .as(AdvancedStream.type())
                .groupBy(integer -> integer % 3)
                .forEach(System.out::println);
    }

    private static void testIteratorAfterFlatMap() {
        AtomicInteger count = new AtomicInteger();
        Streamable.from(Stream.of(1))
                .as(JavaStream.type())
                .flatMap(d -> Streamable.from(Stream.generate(Math::random)))
                .as(JavaStream.type())
                .limit(10)
                .flatMap(d -> Streamable.from(Stream.of(d, d, d, d, d)))
                .iterator()
                .forEachRemaining(aDouble -> {
                    count.incrementAndGet();
                });
        System.out.println(count.get());
    }

    private static void testCount() {
        Random random = new Random();
        Streamable.from(Stream.generate(() -> random.nextInt(100)))
                .as(JavaStream.type())
                .limit(10000)
                .as(AdvancedStream.type())
                .count()
                .as(JavaStream.type())
                .flatMap(Map::entrySet)
                .as(JavaStream.type())
                .limit(10)
                .forEach(System.out::println);
    }

    private static void testMapMulti() {
        long count = Streamable.from(Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                .as(JavaStream.type())
                .<Integer> mapMulti((integer, consumer) -> {
                    consumer.accept(integer);
                    consumer.accept(integer);
                    consumer.accept(integer);
                })
                .limit(10)
                .count();
        System.out.println(count);
    }

    private static void testTry() {
        Streamable.from(Stream.of("0", "a", "2", "1"))
                .as(TryingStream.type())
                .tryIt(Integer::parseInt)
                .tryIt(integer -> integer / (integer - 1))
                .keepSuccesssfulAndUnwrap()
                .forEach(System.out::println);
    }

    public static void testFindFirst() {
        Random random = new Random();
        Streamable.from(Stream.generate(() -> random.nextInt(1000)))
                .as(AdvancedStream.type())
                .elementCount(count -> System.out.println(": " + count))
                .as(JavaStream.type())
                .filter(integer -> integer >= 990)
                .findFirst()
                .ifPresent(System.out::println);
    }
}
