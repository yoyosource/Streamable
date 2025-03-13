package de.yoyosource.streamable;

import static de.yoyosource.streamable.streams.ComparableStream.ComparableStream;
import static de.yoyosource.streamable.streams.NumberStream.*;

public class Test {

    public static void main(String[] args) {
        SummaryStatistics<Integer> summaryStatistics = Streamable.of("Hello World", "Hello World 2")
                .map(String::length)
                .as(NumberStream())
                .summaryStatistics();
        System.out.println(summaryStatistics);
    }
}
