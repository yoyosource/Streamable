package de.yoyosource;

import de.yoyosource.streamable.Evaluation2;

import java.util.Arrays;

public class Test {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(Evaluation2.values()));
        System.out.println(Evaluation2.valueOf("ORDERED"));
        System.out.println(Evaluation2.UNORDERED);
    }
}
