package de.yoyosource;

import de.yoyosource.streamable.internal.SplittedQueue;

public class Test5 {

    public static void main(String[] args) {
        SplittedQueue<Integer> splittedQueue = new SplittedQueue<>();
        for (int i = 0; i < 1_000_000_000; i++) {
            splittedQueue.add(i);
        }
        // for (int i = 0; i < 1_000_000_000; i++) {
        //     System.out.println(splittedQueue.remove());
        // }
        System.out.println(splittedQueue);
    }
}
