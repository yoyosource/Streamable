package de.yoyosource.streamable2;

import de.yoyosource.streamable2.internal.StreamableManager;

import java.util.List;

public class Test {

    public static void main(String[] args) {
        StreamableManager.from(List.of("0", "1", "2").spliterator())
                .forEach(System.out::println);
    }
}
