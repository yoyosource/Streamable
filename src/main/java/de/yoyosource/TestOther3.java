package de.yoyosource;

import de.yoyosource.streamable2.Streamable;

import java.util.Arrays;
import java.util.List;

public class TestOther3 {

    public static void main(String[] args) {
        new Streamable<Integer>(1)
                .map(input -> input * 3)
                .filter(number -> number % 2 == 0)
                .peek(input -> System.out.println(input))
                .peek(__ -> System.out.println(Arrays.toString(args)))
                .map(input -> input * 3)
                .filter(number -> number % 2 == 0) // Issue!
                .flatMap(input -> List.of(input, input))
                .peek(input -> System.out.println(input))
                .map(i -> i * 3L)
                .map(i -> i * 3L)
                .run();
    }
}
