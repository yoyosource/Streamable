package de.yoyosource.streamable2.internal.iter;

public final class ArrayIter<T> extends Iter<T> {
    private final T[] array;
    private int start;
    private final int end;

    public ArrayIter(T[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean hasNext() {
        return start < end;
    }

    @Override
    public T next() {
        return array[start++];
    }

    @Override
    public Iter<T> trySplit() {
        int count = end - start;
        if (count < 2) return null;
        int half = count / 2;
        Iter<T> iter = new ArrayIter<>(array, start, start + half);
        start = start + half;
        return iter;
    }
}
