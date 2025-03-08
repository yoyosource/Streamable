package de.yoyosource.streamable2.internal.iter;

import java.util.Random;

public class Test {

    public static void main(String[] args) {
        Iter<Integer> iter = new Iter<>() {
            private Random random = new Random();

            private int l = 0;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                // for (int i = 0; i < 1_000_000; i++) l++;
                return random.nextInt();
            }
        };

        for (int i = 0; i < 1_000_000; i++) {
            System.out.println(iter.trySplit().countRemaining());
        }
    }
}
