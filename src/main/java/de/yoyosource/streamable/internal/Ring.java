package de.yoyosource.streamable.internal;

import lombok.Getter;

public class Ring<T> {

    @Getter
    private int size = 0;
    private Node<T> previous = null;
    private Node<T> current = null;

    private static class Node<T> {
        private Node<T> next = null;
        private T data = null;

        private Node(T data) {
            this.data = data;
        }
    }

    public synchronized void add(T value) {
        if (current == null) {
            current = new Node<>(value);
            current.next = current;
            synchronized (current.data) {
                current.data.notifyAll();
            }
        } else {
            Node<T> toAdd = new Node<>(value);
            toAdd.next = current.next;
            current.next = toAdd;
        }
        size++;
    }

    public synchronized T getData() {
        if (current == null) {
            throw new UnsupportedOperationException("Ring is empty");
        }
        return current.data;
    }

    public synchronized boolean hasData() {
        return current != null;
    }

    public synchronized void remove() {
        if (current == null) {
            return;
        }
        previous.next = current.next;
        if (current == current.next) {
            previous = null;
            current = null;
        } else {
            current = current.next;
        }
        size--;
    }

    public synchronized void next() {
        if (current == null) {
            return;
        }
        previous = current;
        current = current.next;
    }

    @Override
    public synchronized String toString() {
        Node<T> finish = this.current;
        Node<T> current = finish;
        StringBuilder st = new StringBuilder();
        do {
            st.append(current.data).append(" -> ");
            current = current.next;
        } while (current != finish);
        return st.toString();
    }
}
