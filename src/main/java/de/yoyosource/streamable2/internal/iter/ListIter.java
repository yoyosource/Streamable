package de.yoyosource.streamable2.internal.iter;

import java.util.List;

public final class ListIter<T> extends Iter<T> {

    private final List<T> list;
    private int start;
    private final int end;

    public ListIter(List<T> list, int start, int end) {
        this.list = list;
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean hasNext() {
        return start < end;
    }

    @Override
    public T next() {
        return list.get(start++);
    }

    @Override
    public Iter<T> trySplit() {
        int count = end - start;
        if (count < 2) return null;
        int half = count / 2;
        Iter<T> iter = new ListIter<>(list, start, start + half);
        start = start + half;
        return iter;
    }
}
