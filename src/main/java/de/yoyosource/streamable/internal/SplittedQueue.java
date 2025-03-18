package de.yoyosource.streamable.internal;

import java.util.ArrayList;
import java.util.List;

public class SplittedQueue<T> {

    private static final int ARRAY_SIZE = 100_000;

    private List<T[]> queue = new ArrayList<>();
    private int insertIndex = 0;
    private int removeIndex = 0;

    public void add(T value) {
        if (queue.isEmpty() || insertIndex == ARRAY_SIZE) {
            insertIndex = 0;
            queue.add((T[]) new Object[ARRAY_SIZE]);
        }
        queue.getLast()[insertIndex++] = value;
    }

    public T remove() {
        if (queue.isEmpty()) {
            throw new IndexOutOfBoundsException("Queue is empty");
        }
        if (removeIndex == insertIndex && queue.getFirst() == queue.getLast()) {
            throw new IndexOutOfBoundsException("Queue is empty");
        }
        T element = queue.getFirst()[removeIndex++];
        if (removeIndex == ARRAY_SIZE) {
            removeIndex = 0;
            queue.removeFirst();
        }
        return element;
    }
}
