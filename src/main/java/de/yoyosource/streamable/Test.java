package de.yoyosource.streamable;

import de.yoyosource.streamable.streams.AdvancedStream;
import de.yoyosource.streamable.streams.JavaStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static de.yoyosource.streamable.streams.JavaStream.JavaStream;
import static de.yoyosource.streamable.streams.NumberStream.NumberStream;
import static de.yoyosource.streamable.streams.NumberStream.SummaryStatistics;

public class Test {

    public static void main(String[] args) {
        Random random = new Random();
        Streamable.generate(random::nextBoolean)
                // .parallel(100)
                .limit(1_000_000_000)
                .as(AdvancedStream.AdvancedStream())
                .consecutiveElementCount()
                .as(JavaStream())
                .max(Map.Entry.comparingByValue())
                .ifPresent(System.out::println);
    }
}
