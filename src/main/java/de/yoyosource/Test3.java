package de.yoyosource;

import de.yoyosource.streamable.ThreadManager;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Test3 {

    private static List<Integer> ORDER = Collections.synchronizedList(new ArrayList<>());

    @SneakyThrows
    public static void main(String[] args) {
        // {1=6, 2=8, 3=8, 4=18, 5=17, 6=1, 8=1, 9=7, 10=4, 11=15, 12=4, 13=15, 14=7, 15=3, 16=13, 17=13, 18=7, 19=7, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=19, 5=19, 6=1, 8=1, 9=7, 10=5, 11=15, 12=4, 13=15, 14=7, 15=4, 16=13, 17=13, 18=8, 19=8, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=19, 5=18, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=15, 14=7, 15=3, 16=13, 17=13, 18=8, 19=7, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=13, 3=12, 4=16, 5=18, 6=3, 8=1, 9=10, 10=6, 11=11, 12=6, 13=14, 14=10, 15=5, 16=10, 17=9}
        // {1=6, 2=9, 3=9, 4=18, 5=18, 6=1, 8=1, 9=7, 10=5, 11=15, 12=4, 13=15, 14=7, 15=4, 16=13, 17=13, 18=7, 19=7, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=20, 5=21, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=17, 14=7, 15=4, 16=15, 17=14, 18=9, 19=8, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=18, 5=18, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=15, 14=7, 15=3, 16=13, 17=12, 18=8, 19=7, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=20, 5=21, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=17, 14=7, 15=4, 16=15, 17=14, 18=9, 19=8, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=18, 5=18, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=15, 14=7, 15=3, 16=13, 17=12, 18=8, 19=7, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=20, 5=21, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=17, 14=7, 15=4, 16=15, 17=14, 18=9, 19=8, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=19, 5=19, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=15, 14=6, 15=3, 16=13, 17=13, 18=8, 19=7, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=20, 5=21, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=17, 14=7, 15=4, 16=15, 17=14, 18=9, 19=8, 20=4, 21=4, 22=3, 23=1}
        // {1=6, 2=8, 3=8, 4=18, 5=17, 6=1, 8=1, 9=7, 10=4, 11=15, 12=4, 13=15, 14=7, 15=3, 16=13, 17=13, 18=8, 19=8, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=19, 5=18, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=15, 14=6, 15=3, 16=12, 17=12, 18=8, 19=7, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=20, 5=21, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=17, 14=7, 15=4, 16=15, 17=14, 18=8, 19=9, 20=4, 21=4, 22=4, 23=1}
        // {1=6, 2=9, 3=9, 4=18, 5=18, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=15, 14=7, 15=3, 16=13, 17=13, 18=7, 19=7, 20=3, 21=3, 22=3, 23=1}
        // {1=6, 2=9, 3=9, 4=18, 5=18, 6=1, 8=1, 9=8, 10=5, 11=15, 12=4, 13=15, 14=7, 15=3, 16=13, 17=12, 18=8, 19=7, 20=3, 21=3, 22=3, 23=1}

        ThreadManager tm = new ThreadManager();
        tm.setMaxThreadIdleTime(50);
        tm.setMaxWorkIdleTime(1000);

        AtomicInteger element = new AtomicInteger(0);
        List<ThreadManager.QueueKey> keys = new ArrayList<>();
        long time = System.currentTimeMillis();
        Random random = new Random(0);
        while (System.currentTimeMillis() - time < 30_000) {
            Thread.sleep(10);
            if (random.nextDouble() <= 0.98) continue;
            if (random.nextBoolean()) {
                if (keys.isEmpty()) continue;
                System.out.println("Dequeue Element");
                keys.get(random.nextInt(keys.size())).dequeue();
            } else {
                System.out.println("Queue Element");
                long sleep = random.nextLong(900) + 100;
                int orderId = element.incrementAndGet();
                keys.add(tm.queue(() -> {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                    }
                    ORDER.add(orderId);
                }, random.nextInt(3) + 1));
            }
        }
        /*
        System.out.println(": " + tm.getNumberOfThreads());
        keys.forEach(ThreadManager.QueueKey::dequeue);

        while (tm.getNumberOfThreads() > 0) {
            Thread.yield();
        }
         */

        System.out.println(ORDER.size());
        Map<Integer, Long> counts = ORDER.stream().collect(Collectors.toMap(k -> k, k -> 1L, Long::sum));
        System.out.println(counts);
    }
}
