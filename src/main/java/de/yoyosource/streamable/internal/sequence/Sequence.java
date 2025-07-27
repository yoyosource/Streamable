package de.yoyosource.streamable.internal.sequence;

import java.util.Iterator;

public interface Sequence<T> extends Iterable<T>, Iterator<T> {

    Inserter<T> inserter();

    boolean isEmpty();

    default Iterator<T> iterator() {
        return this;
    }

    interface Inserter<T> {

        Inserter<T> add(T value);
        void release();
    }
}
