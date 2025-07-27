package de.yoyosource.streamable;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ThreadManager {

    private static final AtomicInteger THREAD_MANAGER_ID = new AtomicInteger();

    private static final ThreadManager GLOBAL = new ThreadManager();
    private static final ThreadLocal<ThreadManager> LOCAL = ThreadLocal.withInitial(() -> GLOBAL);

    public static void setThreadManager(final ThreadManager threadManager) {
        if (threadManager == null) {
            LOCAL.remove();
        } else {
            LOCAL.set(threadManager);
        }
    }

    public static QueueKey queueToCurrent(Runnable runnable, int concurrentInstances) {
        return LOCAL.get().queue(runnable, concurrentInstances);
    }

    private final Thread manager;

    @Getter
    private final String name;
    private List<Worker> workers = new ArrayList<>();
    private final List<QueueKey> work = new ArrayList<>();

    private final AtomicInteger workerThreadIds = new AtomicInteger();

    @Setter
    private long maxWorkIdleTime = 50;
    @Setter
    private long maxThreadIdleTime = 1000;
    @Setter
    private int maxNumberOfThreads = 5_000;

    public ThreadManager() {
        int num = THREAD_MANAGER_ID.getAndIncrement();
        if (num == 0) {
            name = "GlobalThreadManager";
        } else {
            name = "ThreadManager" + num;
        }

        manager = new Thread(this::run);
        manager.setDaemon(true);
        manager.setName(name + "-Manager");
        manager.start();
    }

    public QueueKey queue(Runnable runnable, int concurrentInstances) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        QueueKey queueKey = new QueueKey(runnable, concurrentInstances, elements);
        synchronized (work) {
            work.add(queueKey);
        }
        return queueKey;
    }

    public void close() {
        if (this == GLOBAL) return;
        if (LOCAL.get() == this) {
            LOCAL.remove();
        }
        synchronized (work) {
            work.clear();
        }
        synchronized (workers) {
            workers.forEach(Thread::interrupt);
            workers.clear();
        }
        manager.interrupt();
    }

    public static final class QueueKey {
        private final Runnable runnable;
        private AtomicInteger running = new AtomicInteger();
        private final StackTraceElement[] elements;

        public QueueKey(Runnable runnable, int concurrentInstances, StackTraceElement[] elements) {
            this.runnable = runnable;
            running.set(concurrentInstances);
            this.elements = elements;
        }

        private boolean dequeued = false;
        private long lastFinish = System.currentTimeMillis();

        public void dequeue() {
            dequeued = true;
        }

        @Override
        public String toString() {
            return "QueueKey{" +
                    "runnable=" + runnable +
                    ", running=" + running +
                    ", dequeued=" + dequeued +
                    ", lastFinish=" + lastFinish +
                    '}';
        }
    }

    private void run() {
        // long time = System.currentTimeMillis();
        while (true) {
            long now = System.currentTimeMillis();
            // boolean debugOutput = now - time > 1000;
            // if (debugOutput) {
            //     time = now;
            // }
            // if (debugOutput) {
            //     synchronized (work) {
            //         System.out.println(work.size() + ": " + work);
            //     }
            // }
            // Remove all Dead Thread!
            workers.removeIf(worker -> !worker.isAlive());

            // Check if anything needs to be dequeued
            List<QueueKey> dequeuedQueueKeys = new ArrayList<>();
            synchronized (work) {
                // if (debugOutput) {
                //     System.out.println("Removing anything that is dequeued");
                // }
                work.removeIf(queueKey -> {
                    if (!queueKey.dequeued) return false;
                    dequeuedQueueKeys.add(queueKey);
                    return true;
                });
            }
            // Interrupt all Threads that should be dequeued and remove them from the workers list
            dequeuedQueueKeys.forEach(queueKey -> {
                List<Worker> toRemove = workers.stream()
                        .filter(worker -> worker.currentWork.get() == queueKey)
                        .toList();
                for (Worker worker : toRemove) {
                    worker.interrupt();
                    workers.remove(worker);
                    // System.out.println("Remove worker: " + worker);
                }
            });

            // Retrieve all open work from most important to run next to least important
            List<QueueKey> openWork;
            synchronized (work) {
                // if (debugOutput) {
                //     System.out.println("Selecting anything that is currently not running");
                // }
                openWork = work.stream()
                        .filter(queueKey -> queueKey.running.get() > 0)
                        .sorted(Comparator.<QueueKey>comparingInt(value -> -value.running.get())
                                .thenComparingLong(value -> value.lastFinish))
                        .collect(Collectors.toList());
            }

            // Retrieve all open workers without anything to do
            List<Worker> openWorkers = workers.stream()
                    .filter(worker -> worker.currentWork.get() == null)
                    .collect(Collectors.toList());

            // System.out.println(openWork + " " + openWorkers);

            // Assign everything to the openWorkers until no work or no workers are left
            while (!openWork.isEmpty() && !openWorkers.isEmpty()) {
                QueueKey queueKey = openWork.removeFirst();
                Worker worker = openWorkers.removeFirst();
                worker.setWork(queueKey);
            }

            // Check for every remaining work and create a Worker if absolutely necessary (see maxWorkIdleTime)
            for (QueueKey queueKey : openWork) {
                if (now - queueKey.lastFinish > maxWorkIdleTime) {
                    if (workers.size() >= maxNumberOfThreads) {
                        break;
                    }
                    Worker worker = new Worker(this);
                    worker.setWork(queueKey);
                    workers.add(worker);
                    // System.out.println("Add worker: " + worker);
                } else {
                    break;
                }
            }

            // Remove any worker not having something to do for more than maxThreadIdleTime
            for (Worker worker : openWorkers) {
                if (now - worker.lastFinish > maxThreadIdleTime) {
                    worker.interrupt();
                    workers.remove(worker);
                    // System.out.println("Remove worker: " + worker);
                }
            }
        }
    }

    private static final class Worker extends Thread {

        private final AtomicReference<QueueKey> currentWork = new AtomicReference<>(null);
        private long lastFinish = System.currentTimeMillis();

        public Worker(ThreadManager manager) {
            setDaemon(true);
            setName(manager.name + "-Worker-" + manager.workerThreadIds.getAndIncrement());
            start();
        }

        public void setWork(QueueKey queueKey) {
            if (!this.isAlive() || this.isInterrupted()) return;
            // System.out.println(getName() + "=" + queueKey);
            queueKey.running.decrementAndGet();
            currentWork.set(queueKey);
        }

        @Override
        public void run() {
            try {
                while (!this.isInterrupted()) {
                    QueueKey queueKey = currentWork.get();
                    if (queueKey == null) {
                        Thread.yield();
                        continue;
                    }

                    try {
                        queueKey.runnable.run();
                    } catch (Throwable t) {
                        if (t instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    queueKey.lastFinish = System.currentTimeMillis();
                    this.lastFinish = System.currentTimeMillis();
                    currentWork.set(null);
                    queueKey.running.incrementAndGet();
                }
            } catch (Throwable t) {
                // Ignore
            }
        }
    }
}
