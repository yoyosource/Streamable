package de.yoyosource.streamable.internal.sequence;

import de.yoyosource.streamable.internal.FinishException;

public class UnorderedSequence<T> implements Sequence<T>, Sequence.Inserter<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size = 0;
    private boolean finished = false;

    @Override
    public Inserter<T> inserter() {
        return this;
    }

    @Override
    public void finish() {
        finished = true;
    }

    @Override
    public synchronized Inserter<T> add(T value) {
        if (finished || (tail == null && head != null)) {
            throw FinishException.INSTANCE;
        }
        Node<T> node = new Node<>(value);
        if (head == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
        return this;
    }

    @Override
    public void release() {
    }

    @Override
    public void cutShort() {
        tail = null;
    }

    @Override
    public synchronized boolean isEmpty() {
        return head == null;
    }

    @Override
    public synchronized boolean hasNext() {
        return head != null;
    }

    @Override
    public synchronized T next() {
        T value = head.value;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return value;
    }

    @Override
    public synchronized T peek() {
        if (head == null) return null;
        return head.value;
    }

    @Override
    public synchronized void remove() {
        if (head == null) return;
        head = head.next;
        if (head == null) tail = null;
        size--;
    }

    @Override
    public int size() {
        return size;
    }

    private static class Node<T> {

        private final T value;
        protected Node<T> next = null;

        public Node(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "value=" + value +
                    '}';
        }
    }
}
