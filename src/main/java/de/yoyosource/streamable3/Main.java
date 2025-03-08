package de.yoyosource.streamable3;

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
        for (int i = 0; i < 10000; i++) {
            strings.add(generateRandomString(10));
        }

        List<Element.Value<String>> elements = new ArrayList<>();
        long index = 0;
        for (String s : strings) {
            elements.add(new Element.Value<>(index++, s));
        }

        StreamableGathererStep2 step = new StreamableGathererStep2(new StreamableGatherer<String, AtomicLong, Long>() {
            @Override
            public AtomicLong container() {
                return new AtomicLong();
            }

            @Override
            public boolean integrate(AtomicLong container, Element.Value<String> element, Consumer<? super Long> next) {
                try {
                    Thread.sleep(RANDOM.nextInt(1000) + 500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                container.incrementAndGet();
                // System.out.println("Element: " + element);
                return false;
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
        });
        step.setNext(new StreamableGathererStep2(new StreamableGatherer<Long, Object, String>() {
            @Override
            public Object container() {
                return null;
            }

            @Override
            public boolean integrate(Object container, Element.Value<Long> element, Consumer<? super String> next) {
                next.accept("Count: " + element.value());
                return false;
            }

            @Override
            public Object combine(Object firstContainer, Object secondContainer) {
                return null;
            }

            @Override
            public void finish(Object container, Consumer<? super String> next) {
            }
        })).setNext(new StreamableGathererStep2(new StreamableGatherer<String, Object, String>() {
            @Override
            public Object container() {
                return null;
            }

            @Override
            public boolean integrate(Object container, Element.Value<String> element, Consumer<? super String> next) {
                // System.out.println(element);
                if (element.index() % 2 == 0) next.accept(element.value());
                return false;
            }

            @Override
            public Object combine(Object firstContainer, Object secondContainer) {
                return null;
            }

            @Override
            public void finish(Object container, Consumer<? super String> next) {

            }
        })).setNext(Step.Printer.INSTANCE);

        elements.forEach(step::consume);
        step.consume(new Element.Finish());

        Thread.sleep(120_000);
    }
}
