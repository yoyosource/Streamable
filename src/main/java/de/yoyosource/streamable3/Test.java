package de.yoyosource.streamable3;

public class Test {

    public static void main(String[] args) {
        Streamable.of("Hello World")
                .map(s -> "1 " + s)
                .findFirst()
                .ifPresent(System.out::println);
    }
}
