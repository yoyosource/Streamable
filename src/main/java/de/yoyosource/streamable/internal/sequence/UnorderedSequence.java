package de.yoyosource.streamable.internal.sequence;

public class UnorderedSequence<T> implements Sequence<T>, Sequence.Inserter<T> {

    private Node<T> head;
    private Node<T> tail;

    @Override
    public Inserter<T> inserter() {
        return this;
    }

    @Override
    public synchronized Inserter<T> add(T value) {
        Node<T> node = new Node<>(value);
        if (head == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        return this;
    }

    @Override
    public void release() {
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
        return value;
    }

    private static class Node<T> {

        private T value;
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
