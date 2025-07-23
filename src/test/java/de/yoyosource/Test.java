package de.yoyosource;

import de.yoyosource.streamable.Streamable;

public class Test {

    public static void main(String[] args) {
        int result = Streamable.of(1, 2, 3)
                .parallel(3)
                .map(integer -> {
                    try {
                        if (integer == 2) {
                            Thread.sleep(1000);
                        } else {
                            Thread.sleep(2000);
                        }
                        // Thread.sleep(10000 - integer * 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return integer * 2;
                })
                .findAny()
                .orElseThrow();
                // .forEach(System.out::println);
        System.out.println(result);
    }
}
