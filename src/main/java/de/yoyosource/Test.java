package de.yoyosource;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.impl.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Test {

    public static void main(String[] args) {
        testSimpleForEach();
        testGroupBy();
        testIteratorAfterFlatMap();
        testCountAfterFlatMap();
        testCount();
        testMapMulti();
        testTry();
        testFindFirst();
        testSlidingWindow();
        testFixedWindow();
        testExplodingFlatMap();
        // testPrimeGenerator();
        // testDoubleMutate();
        testStreamConcat();
        testComparableStream();

        if (true) return;

        Streamable.of(1, 2, 3)
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
        for (int i : Streamable.of(0, 1, 2)) {
            System.out.println(i);
        }

        System.out.println();
        long count = Streamable.of(1)
                .as(JavaStream.type())
                .flatMap(d -> Streamable.from(Stream.generate(Math::random)))
                .limit(10)
                .count();
        System.out.println(count);

        Streamable.of(1)
                .as(JavaStream.type())
                .flatMap(d -> Streamable.from(Stream.generate(Math::random)))
                .limit(10)
                .iterator()
                .forEachRemaining(System.out::println);
    }

    private static void testSimpleForEach() {
        Streamable.of(1, 2, 3)
                .forEach(System.out::println);
    }

    private static void testGroupBy() {
        Streamable.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .as(AdvancedStream.type())
                .groupBy(integer -> integer % 3)
                .forEach(System.out::println);
    }

    private static void testIteratorAfterFlatMap() {
        AtomicInteger count = new AtomicInteger();
        Streamable.of(1)
                .as(JavaStream.type())
                .flatMap(d -> Streamable.from(Stream.generate(Math::random)))
                .limit(10)
                .flatMap(d -> Streamable.from(Stream.of(d, d, d, d, d)))
                .mapMulti((aDouble, consumer) -> {
                    consumer.accept(aDouble);
                    consumer.accept(aDouble);
                    consumer.accept(aDouble);
                    consumer.accept(aDouble);
                    consumer.accept(aDouble);
                })
                .iterator()
                .forEachRemaining(aDouble -> {
                    count.incrementAndGet();
                });
        System.out.println(count.get());
    }

    private static void testCountAfterFlatMap() {
        long count = Streamable.of(1)
                .as(JavaStream.type())
                .flatMap(d -> Streamable.from(Stream.generate(Math::random)))
                .limit(10)
                .flatMap(d -> Streamable.from(Stream.of(d, d, d, d, d)))
                .count();
        System.out.println(count);
    }

    private static void testCount() {
        Random random = new Random();
        Streamable.generate(() -> random.nextInt(100))
                .as(JavaStream.type())
                .limit(10000)
                .as(AdvancedStream.type())
                .count()
                .as(JavaStream.type())
                .flatMap(Map::entrySet)
                .limit(10)
                .forEach(System.out::println);
    }

    private static void testMapMulti() {
        long count = Streamable.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .as(JavaStream.type())
                .<Integer>mapMulti((integer, consumer) -> {
                    consumer.accept(integer);
                    consumer.accept(integer);
                    consumer.accept(integer);
                })
                .limit(10)
                .count();
        System.out.println(count);
    }

    private static void testTry() {
        Streamable.of("0", "a", "2", "1")
                .as(TryingStream.type())
                .tryIt(Integer::parseInt)
                .tryIt(integer -> integer / (integer - 1))
                .keepAndUnwrap(TryedStream.successful())
                .forEach(System.out::println);
    }

    public static void testFindFirst() {
        Random random = new Random();
        Streamable.generate(() -> random.nextInt(1000))
                .as(AdvancedStream.type())
                .elementCount(count -> System.out.println(": " + count))
                .as(JavaStream.type())
                .filter(integer -> integer >= 990)
                .findFirst()
                .ifPresent(System.out::println);
    }

    public static void testSlidingWindow() {
        System.out.println();
        Random random = new Random();
        Streamable.generate(() -> random.nextInt(1000))
                .as(JavaStream.type())
                .limit(20)
                .as(AdvancedStream.type())
                .windowSliding(10)
                .forEach(integers -> System.out.println(integers.size() + " " + integers));
    }

    public static void testFixedWindow() {
        System.out.println();
        Random random = new Random();
        Streamable.generate(() -> random.nextInt(1000))
                .as(JavaStream.type())
                .limit(30)
                .as(AdvancedStream.type())
                .windowFixed(10)
                .forEach(integers -> System.out.println(integers.size() + " " + integers));
    }

    public static void testPrimeGenerator() {
        if (false) {
            long time = System.currentTimeMillis();
            long num = 1;
            long[] ints = new long[1_000_000];
            ints[0] = 2;
            int index = 1;
            outer:
            while (System.currentTimeMillis() - time < 30_000) {
                num += 2;
                for (int i = 0; i < index; i++) {
                    if (num % ints[i] == 0) continue outer;
                }
                ints[index++] = num;
            }
            System.out.println(index);
        }

        if (true) {
            Streamable.of(2)
                    .as(AdvancedStream.type())
                    .concat(Streamable.iterate(3, integer -> integer + 2))
                    .<Integer, Streamable<Integer>>gather(new StreamableGatherer<>() {
                        private int[] ints = new int[1_000_000];
                        private int index = 0;

                        @Override
                        public boolean apply(Integer input, Consumer<Integer> next) {
                            for (int i = 0; i < index; i++) {
                                if (input % ints[i] == 0) return false;
                            }
                            next.accept(input);
                            ints[index++] = input;
                            return false;
                        }

                        @Override
                        public void finish(Consumer<Integer> next) {
                        }
                    })
                    .collect(new StreamableCollector<>() {
                        private long count = 0;
                        private long time = System.currentTimeMillis();

                        @Override
                        public boolean apply(Integer input) {
                            count++;
                            // 10_000 -> 86022
                            // 10_000 -> 77141
                            // 10_000 -> 94862
                            // 10_000 -> 182324
                            // 30_000 -> 316893
                            return System.currentTimeMillis() - time > 10_000;
                        }

                        @Override
                        public Object finish() {
                            System.out.println("Count: " + count);
                            return null;
                        }
                    });
        }
    }

    public static void testExplodingFlatMap() {
        Streamable.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .as(AdvancedStream.type())
                .flatMapMulti((integer, iterableConsumer) -> {
                    for (int i = 0; i < integer; i++) {
                        iterableConsumer.accept(List.of(i));
                    }
                })
                .forEach(System.out::println);
    }

    public static void testDoubleMutate() {
        JavaStream<Integer> streamable = Streamable.of(1, 2, 3)
                .as(JavaStream.type());
        streamable.map(integer -> integer * 2);
        streamable.filter(integer -> integer % 2 == 0);
    }

    public static void testStreamConcat() {
        long count = Streamable.of(1, 2, 3, 4, 5)
                .as(AdvancedStream.type())
                .concat(Streamable.of(6, 7, 8, 9, 10))
                .as(JavaStream.type())
                .count();
        System.out.println(count);
    }

    public static void testComparableStream() {
        Streamable.of("Hello", "World")
                .as(ComparableStream.type())
                .min()
                .ifPresent(System.out::println);
    }

    public static void testScanStream() {
        Streamable.iterate(BigInteger.ONE, bigInteger -> bigInteger.add(BigInteger.ONE))
                .as(AdvancedStream.type())
                .scan(BigInteger::add)
                .as(JavaStream.type())
                .limit(1000)
                .findLast()
                .ifPresent(System.out::println);
    }

    public static void testOptionalStream() {
        Streamable.of(Optional.of(0))
                .as(OptionalStream.type())
                .isPresent()
                .get()
                .forEach(System.out::println);
    }
}
