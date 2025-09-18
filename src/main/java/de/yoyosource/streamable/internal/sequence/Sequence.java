package de.yoyosource.streamable.internal.sequence;

import java.util.Iterator;

public interface Sequence<T> extends Iterable<T>, Iterator<T> {

    Inserter<T> inserter();

    boolean isEmpty();

    default Iterator<T> iterator() {
        return this;
    }

    T peek();

    default T poll() {
        if (hasNext()) {
            return next();
        } else {
            return null;
        }
    }

    int size();

    interface Inserter<T> {

        Inserter<T> add(T value);
        void release();
        void cutShort();
    }
}
