package de.yoyosource.streamable.internal.sequence;

public class OrderedSequence<T> implements Sequence<T> {

    private Node<T> head;
    private Node<T> tail;

    public OrderedSequence() {
        head = tail = new BreakerNode<>();
    }

    @Override
    public String toString() {
        Node<T> current = this.current != null ? this.current : head;
        StringBuilder sb = new StringBuilder();
        while (current != null) {
            if (current == this.current) {
                sb.append("*");
            }
            if (current instanceof OrderedSequence.ElementNode<T> elementNode) {
                sb.append(elementNode.value);
            } else {
                sb.append("[").append(current.released).append("]");
            }
            if (current.next != null) {
                sb.append(" -> ");
            }
            current = current.next;
        }
        return sb.toString();
    }

    public synchronized Inserter<T> inserter() {
        Node<T> current = tail;
        tail.next = new BreakerNode<>();
        tail = tail.next;
        return new InserterImpl<>(current);
    }

    public boolean isEmpty() {
        return head.next == null;
    }

    private ElementNode<T> _getNext() {
        while (head instanceof OrderedSequence.BreakerNode<T> breakerNode && breakerNode.released) {
            head = head.next;
        }
        if (head instanceof OrderedSequence.ElementNode<T> elementNode) {
            head = head.next;
            return elementNode;
        }
        return null;
    }

    private ElementNode<T> current = null;

    @Override
    public synchronized boolean hasNext() {
        if (current == null) {
            current = _getNext();
        }
        return current != null;
    }

    @Override
    @SuppressWarnings("java:S2272")
    public synchronized T next() {
        T value = current.value;
        current = null;
        return value;
    }

    @Override
    public synchronized T peek() {
        if (current == null) {
            current = _getNext();
        }
        if (current == null) {
            return null;
        }
        return current.value;
    }

    @Override
    public synchronized void remove() {
        if (current == null) {
            _getNext();
        } else {
            current = null;
        }
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
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

    private static class InserterImpl<T> implements Inserter<T> {
        private BreakerNode<T> first;
        private Node<T> current;

        private InserterImpl(Node<T> current) {
            if (!(current instanceof OrderedSequence.BreakerNode<T>)) {
                throw new IllegalArgumentException("Inserter requires a Sequence.BreakerNode");
            }
            this.first = (OrderedSequence.BreakerNode<T>) current;
            this.current = current;
        }

        public synchronized InserterImpl<T> add(T value) {
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
        }
    }
}
