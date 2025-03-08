package de.yoyosource.streamable3;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

// Median: 158ms
// Average: 248.73ms
// Min: 16ms
// Max: 2764ms
// Total: 24873ms

// Median: 150ms
// Average: 222.59ms
// Min: 16ms
// Max: 1178ms
// Total: 22259ms
public class Sequence<T> implements Iterable<T>, Iterator<T> {

    private final AtomicLong unreleasedInserter = new AtomicLong();

    private long index = -1;
    private Node<T> head;
    private Node<T> tail;

    public Sequence() {
        head = tail = new BreakerNode<>();
    }

    public synchronized Inserter<T> inserter() {
        unreleasedInserter.incrementAndGet();
        Node<T> current = tail;
        tail.next = new BreakerNode<>();
        tail = tail.next;
        return new Inserter<>(current, unreleasedInserter);
    }

    public boolean isEmpty() {
        try {
            System.out.println(head.next.next.next.next);
        } catch (Exception e) {

        }
        return head.next == null;
    }

    public boolean hasUnreleasedInserter() {
        return unreleasedInserter.get() > 0;
    }

    private ElementNode<T> _getNext() {
        if (!head.released) {
            return null;
        }
        Node<T> current = head.next;
        if (current == null) {
            return null;
        }

        head = current;
        if (!(head instanceof Sequence.ElementNode<T> elementNode)) {
            return null;
        }

        index++;
        head = head.next;
        return elementNode;
    }

    @Override
    @SuppressWarnings("java:S4348")
    public Iterator<T> iterator() {
        return this;
    }

    private ElementNode<T> current = null;

    @Override
    public boolean hasNext() {
        if (current == null) {
            current = _getNext();
        }
        return current != null;
    }

    @Override
    @SuppressWarnings("java:S2272")
    public T next() {
        T value = current.value;
        current = null;
        return value;
    }

    public long index() {
        return index;
    }

    private abstract static class Node<T> {
        protected Node<T> next = null;

        protected volatile boolean released = false;
    }

    private static class BreakerNode<T> extends Node<T> {

        @Override
        public String toString() {
            return "BreakerNode{" +
                    "released=" + released +
                    '}';
        }
    }

    private static class ElementNode<T> extends Node<T> {

        private T value;

        public ElementNode(T value) {
            this.value = value;
            this.released = true;
        }

        @Override
        public String toString() {
            return "ElementNode{" +
                    "value=" + value +
                    '}';
        }
    }

    public static class Inserter<T> {
        private final AtomicLong unreleasedInserter;
        private BreakerNode<T> first;
        private Node<T> current;

        private Inserter(Node<T> current, AtomicLong unreleasedInserter) {
            if (!(current instanceof Sequence.BreakerNode<T>)) {
                throw new IllegalArgumentException("Inserter requires a Sequence.BreakerNode");
            }
            this.first = (Sequence.BreakerNode<T>) current;
            this.current = current;
            this.unreleasedInserter = unreleasedInserter;
        }

        public synchronized Inserter<T> add(T value) {
            ElementNode<T> newNode = new ElementNode<>(value);
            newNode.next = current.next;
            current.next = newNode;
            current = newNode;
            return this;
        }

        public synchronized void release() {
            first.released = true;
            first = null;
            current = null;
            unreleasedInserter.decrementAndGet();
        }
    }
}
