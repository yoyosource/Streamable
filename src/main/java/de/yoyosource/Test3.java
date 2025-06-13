package de.yoyosource;

import de.yoyosource.streamable.internal.ThreadManager;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Test3 {

    private static List<Integer> ORDER = Collections.synchronizedList(new ArrayList<>());

    @SneakyThrows
    public static void main(String[] args) {
        List<ThreadManager.QueueKey> keys = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            keys.add(ThreadManager.queue(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                ORDER.add(1);
            }));
        }
        for (int i = 0; i < 2; i++) {
            keys.add(ThreadManager.queue(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
                ORDER.add(2);
            }));
        }
        for (int i = 0; i < 3; i++) {
            keys.add(ThreadManager.queue(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
                ORDER.add(3);
            }));
        }
        for (int i = 0; i < 4; i++) {
            keys.add(ThreadManager.queue(() -> {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                }
                ORDER.add(4);
            }));
        }
        for (int i = 0; i < 5; i++) {
            keys.add(ThreadManager.queue(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                ORDER.add(5);
            }));
        }

        Thread.sleep(100000);
        keys.forEach(ThreadManager.QueueKey::dequeue);
        Thread.sleep(1000);
        System.out.println(ThreadManager.getNumberOfThreads());

        System.out.println(ORDER.size());
        Map<Integer, Long> counts = ORDER.stream().collect(Collectors.toMap(k -> k, k -> 1L, Long::sum));
        System.out.println(counts);
    }
}
