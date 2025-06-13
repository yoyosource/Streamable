package de.yoyosource.streamable.internal;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadManager {

    private static final Object LOCK = new Object();
    private static final AtomicInteger queueKeyCounter = new AtomicInteger();
    private static Node<QueueKey> currentNode = null;
    private static final AtomicInteger threadCounter = new AtomicInteger();
    private static Node<Thread> currentThread = null;

    public static final class QueueKey {

        private final Runnable runnable;
        private boolean dequeued = false;
        private long lastFinish = System.currentTimeMillis();
        private boolean running = false;

        private QueueKey(Runnable runnable) {
            this.runnable = runnable;
        }

        public void dequeue() {
            queueKeyCounter.decrementAndGet();
            dequeued = true;
        }
    }

    private static final class Node<T> {
        private final T value;
        private Node<T> next;

        public Node(T value) {
            this.value = value;
        }
    }

    private static final class Worker extends Thread {

        public Worker() {
            setName("Worker-" + threadCounter.incrementAndGet());
            setDaemon(true);
            start();
        }

        @Override
        public void run() {
            while (true) {
                // Current Thread not active!
                if (currentThread.value != this) {
                    Thread.yield();
                    continue;
                }

                // No work to do
                if (currentNode == null) {
                    Thread.yield();
                    continue;
                }

                // Dequeue dequeued elements!
                while (currentNode.next.value.dequeued) {
                    if (currentNode == currentNode.next) {
                        currentNode = null;
                        break;
                    } else {
                        currentNode.next = currentNode.next.next;
                    }
                }

                // No work to do
                if (currentNode == null) {
                    Thread.yield();
                    continue;
                }

                // Check if current QueueKey is already running
                QueueKey queueKey = currentNode.value;
                if (queueKey.running) {
                    currentNode = currentNode.next;
                    continue;
                }

                queueKey.running = true;
                currentNode = currentNode.next;

                if (!currentNode.value.running && System.currentTimeMillis() - currentNode.value.lastFinish > 50) {
                    createThread();
                }

                currentThread = currentThread.next;
                queueKey.runnable.run();
                queueKey.lastFinish = System.currentTimeMillis();
                queueKey.running = false;
            }
        }
    }

    private static synchronized void createThread() {
        if (threadCounter.get() > 5_000) return;
        if (currentThread == null) {
            currentThread = new Node<>(new Worker());
            currentThread.next = currentThread;
        } else {
            Node<Thread> temp = currentThread.next;
            currentThread.next = new Node<>(new Worker());
            currentThread.next.next = temp;
        }
        // System.out.println("Creating thread " + threadCounter.get());
    }

    public static synchronized QueueKey queue(Runnable runnable) {
        if (currentThread == null) {
            createThread();
        }

        QueueKey queueKey = new QueueKey(runnable);
        queueKeyCounter.incrementAndGet();
        synchronized (LOCK) {
            if (currentNode == null) {
                currentNode = new Node<>(queueKey);
                currentNode.next = currentNode;
            } else {
                Node<QueueKey> temp = currentNode.next;
                currentNode.next = new Node<>(queueKey);
                currentNode.next.next = temp;
            }
        }
        return queueKey;
    }

    public static int getNumberOfThreads() {
        return threadCounter.get();
    }
}
