package de.yoyosource.streamable3;

import static de.yoyosource.streamable3.streams.ComparableStream.ComparableStream;
import static de.yoyosource.streamable3.streams.NumberStream.*;

public class Test {

    public static void main(String[] args) {
        SummaryStatistics<Integer> summaryStatistics = Streamable.of("Hello World", "Hello World 2")
                .map(String::length)
                .as(NumberStream())
                .summaryStatistics();
        System.out.println(summaryStatistics);
    }
}
