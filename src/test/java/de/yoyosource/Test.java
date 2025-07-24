package de.yoyosource;

import de.yoyosource.streamable.Streamable;

public class Test {

    public static void main(String[] args) {
        Streamable.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .parallel(5)
                .peek(integer -> {
                    try {
                        Thread.sleep(10000 - integer * 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
                .map(integer -> integer * 2)
                .findAny()
                .ifPresent(System.out::println);
    }
}
