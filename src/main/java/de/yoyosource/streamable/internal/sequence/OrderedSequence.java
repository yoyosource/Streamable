package de.yoyosource.streamable.internal.sequence;

import de.yoyosource.streamable.internal.FinishException;

public class OrderedSequence<T> implements Sequence<T> {

    private Node<T> head;
    private Node<T> tail;
    private boolean finished = false;

    public OrderedSequence() {
        head = tail = new Node<>();
    }

    @Override
    public void finish() {
        finished = true;
    }

    @Override
    public String toString() {
        Node<T> current = head;
        StringBuilder st = new StringBuilder();
        while (current != null) {
            if (current.nodeState == NodeState.WITH_VALUE) {
                st.append(current.value);
            } else {
                st.append("[").append(current.nodeState.name()).append("]");
            }
            if (current.next != null) {
                st.append(" -> ");
            }
            current = current.next;
        }
        return st.toString();
    }

    private enum NodeState {
        WITH_VALUE,
        NO_VALUE,
        CUT_OFF
    }

    private class Node<T> {
        private final T value;
        private NodeState nodeState;
        private Node<T> next = null;

        public Node() {
            this.value = null;
            this.nodeState = NodeState.NO_VALUE;
        }

        public Node(T value) {
            this.value = value;
            this.nodeState = NodeState.WITH_VALUE;
        }
    }

    @Override
    public synchronized Inserter<T> inserter() {
        Node<T> tail = new Node<>();
        Inserter<T> inserter = new InserterImpl<>(this.tail, tail);
        this.tail = tail;
        return inserter;
    }

    @Override
    public synchronized boolean isEmpty() {
        return head.next == null;
    }

    @Override
    public synchronized T peek() {
        while (head.nodeState == NodeState.NO_VALUE && head.next != null) {
            head = head.next;
        }
        if (head.nodeState == NodeState.WITH_VALUE) {
            return head.value;
        } else {
            return null;
        }
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean hasNext() {
        while (head.nodeState == NodeState.NO_VALUE && head.next != null) {
            head = head.next;
        }
        return head.nodeState == NodeState.WITH_VALUE;
    }

    @Override
    public synchronized T next() {
        T value = head.value;
        if (head.next != null) {
            head = head.next;
        } else {
            head.nodeState = NodeState.NO_VALUE;
        }
        return value;
    }

    @Override
    public synchronized void remove() {
        while (head.nodeState == NodeState.NO_VALUE && head.next != null) {
            head = head.next;
        }
        head.nodeState = NodeState.NO_VALUE;
    }

    private class InserterImpl<T> implements Sequence.Inserter<T> {

        private Node<T> current;
        private Node<T> tail;

        public InserterImpl(Node<T> head, Node<T> tail) {
            this.current = head;
            this.tail = tail;
        }

        @Override
        public Inserter<T> add(T value) {
            if (finished) {
                throw FinishException.INSTANCE;
            }
            if (current == null) {
                throw new IllegalStateException();
            }
            Node<T> node = new Node<>(value);
            current.next = node;
            current = node;
            return this;
        }

        @Override
        public void release() {
            if (current == null) {
                throw new IllegalStateException();
            }
            current.next = tail;
            current = null;
            tail = null;
        }

        @Override
        public void cutShort() {
            if (current == null) {
                throw new IllegalStateException();
            }
            Node<T> cutShort = new Node<>();
            cutShort.nodeState = NodeState.CUT_OFF;
            current.next = cutShort;
            tail = null;
        }
    }
}
