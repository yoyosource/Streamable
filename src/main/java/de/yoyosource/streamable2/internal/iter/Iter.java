package de.yoyosource.streamable2.internal.iter;

import java.util.Iterator;

public abstract class Iter<T> implements Iterator<T> {

    private static final int MIN_SPLIT_SIZE = 10_000;
    private static final int MAX_SPLIT_SIZE = 10_000_000;
    private static final int NUMBER_OF_MAX_SPLITS = 50_000;

    public double easeInOutCubic(double x) {
        return x < 0.5 ? 4 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
    }

    private int splitCount = 0;

    public Iter<T> trySplit() {
        int splitSize = (int)(easeInOutCubic(splitCount / (double) NUMBER_OF_MAX_SPLITS) * (MAX_SPLIT_SIZE - MIN_SPLIT_SIZE) + MIN_SPLIT_SIZE);
        T[] array = (T[]) new Object[splitSize];
        int i = 0;
        while (i < splitSize && hasNext()) {
            array[i++] = next();
        }
        if (i == array.length) splitCount++;
        return new ArrayIter<>(array, 0, i);
    }

    public final long countRemaining() {
        long count = 0;
        while (hasNext()) {
            next();
            count++;
        }
        return count;
    }

}
