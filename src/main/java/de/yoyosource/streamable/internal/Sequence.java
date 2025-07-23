package de.yoyosource.streamable.internal;

import java.util.Iterator;

public interface Sequence<T> extends Iterable<T>, Iterator<T> {

    Inserter<T> inserter();

    boolean isEmpty();

    interface Inserter<T> {

        Inserter<T> add(T value);
        void release();
    }
}
