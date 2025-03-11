package de.yoyosource.streamable3;

import de.yoyosource.streamable3.internal.root.Root;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class Main {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            strings.add(generateRandomString(10));
        }

        long time = System.currentTimeMillis();
        String result = new Root(strings.iterator())
                .setNext(1000, new StreamableGatherer<String, AtomicLong, Long>() {
                    @Override
                    public AtomicLong container() {
                        return new AtomicLong();
                    }

                    @Override
                    public boolean integrate(AtomicLong container, long index, String element, Consumer<? super Long> next) {
                        try {
                            Thread.sleep(RANDOM.nextInt(2000) + 500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        container.incrementAndGet();
                        // System.out.println("Element: " + element);
                        return index == 499;
                    }

                    @Override
                    public AtomicLong combine(AtomicLong firstContainer, AtomicLong secondContainer) {
                        firstContainer.addAndGet(secondContainer.get());
                        return firstContainer;
                    }

                    @Override
                    public void finish(AtomicLong container, Consumer<? super Long> next) {
                        next.accept(container.get());
                    }
                })
                .setNext(1, new StreamableGatherer.Simple<Long, String>() {
                    @Override
                    public boolean integrate(long index, Long element, Consumer<? super String> next) {
                        next.accept("Count: " + element);
                        next.accept("Count: " + element);
                        next.accept("Count: " + element);
                        next.accept("Count: " + element);
                        return false;
                    }

                    @Override
                    public void finish(Consumer<? super String> next) {

                    }
                }).setNext(1, new StreamableGatherer.Simple<String, String>() {
                    @Override
                    public boolean integrate(long index, String element, Consumer<? super String> next) {
                        if (index % 2 == 0) next.accept(element);
                        return false;
                    }

                    @Override
                    public void finish(Consumer<? super String> next) {

                    }
                }).setNext(1, new StreamableCollector.Simple<String, String>() {
                    @Override
                    public boolean accumulate(long index, String element) {
                        System.out.println(element);
                        return false;
                    }

                    @Override
                    public String finish() {
                        return "Hello World";
                    }
                });
        time = System.currentTimeMillis() - time;

        System.out.println(result);
        System.out.println("Evaluation took: " + time + "ms");
    }
}
