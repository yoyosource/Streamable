package de.yoyosource.streamable3;

public class Test {

    public static void main(String[] args) {
        // TODO: Add Exception handling on Exception in Streamable Step
        Streamable.of("Hello World")
                .map(s -> "1 " + s)
                .findFirst()
                .ifPresent(System.out::println);
    }
}
