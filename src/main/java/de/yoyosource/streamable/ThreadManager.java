package de.yoyosource.streamable;

import de.yoyosource.streamable.internal.Ring;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

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

    public static ThreadManager getInstance() {
        Thread thread = Thread.currentThread();
        if (thread instanceof Worker) {
            return ((Worker) thread).getThreadManager();
        } else {
            return LOCAL.get();
        }
    }

    @Getter
    private final String name;

    public ThreadManager() {
        int num = THREAD_MANAGER_ID.getAndIncrement();
        if (num == 0) {
            name = "GlobalThreadManager";
        } else {
            name = "ThreadManager" + num;
        }
    }

    private AtomicInteger workerThreadIds = new AtomicInteger();

    @Setter
    private long maxWorkIdleTime = 50;
    @Setter
    private long maxThreadIdleTime = 1000;
    private int maxNumberOfThreads = 5_000;

    public void setMaxNumberOfThreads(int maxNumberOfThreads) {
        if (this == GLOBAL && maxNumberOfThreads > 5_000) {
            maxNumberOfThreads = 5_000;
        }
        this.maxNumberOfThreads = maxNumberOfThreads;
    }

    private final Ring<Worker> workers = new Ring<>();
    private final Ring<QueueKey> work = new Ring<>();

    public static final class QueueKey {

        private final Runnable runnable;
        private boolean dequeued = false;
        private long lastFinish = System.currentTimeMillis();
        private AtomicInteger running = new AtomicInteger();

        private QueueKey(Runnable runnable, int concurrentInstances) {
            this.runnable = runnable;
            running.set(concurrentInstances);
        }

        public void dequeue() {
            dequeued = true;
        }

        @Override
        public String toString() {
            return "QueueKey{" +
                    "runnable=" + runnable +
                    ", dequeued=" + dequeued +
                    ", lastFinish=" + lastFinish +
                    ", running=" + running +
                    '}';
        }
    }

    private final class Worker extends Thread {

        public Worker() {
            setDaemon(true);
            setName(getThreadManager().name + "-Worker-" + workerThreadIds.getAndIncrement());
            start();
        }

        public ThreadManager getThreadManager() {
            return ThreadManager.this;
        }

        @Override
        public void run() {
            long lastRun = System.currentTimeMillis();
            while (true) {
                if (!workers.hasData()) {
                    continue;
                }

                if (workers.getData() != this) {
                    Thread.yield();
                    continue;
                }

                if (!work.hasData()) {
                    // Remove and stop current Thread if idle for longer than a second
                    if (System.currentTimeMillis() - lastRun > maxThreadIdleTime) {
                        workers.remove();
                        return;
                    }

                    Thread.yield();
                    continue;
                }

                // Remove any work that should not run any longer
                if (work.getData().dequeued) {
                    work.remove();
                    continue;
                }

                QueueKey key = work.getData();
                if (key.running.get() == 0) {
                    work.next();
                    continue;
                }

                key.running.decrementAndGet();
                work.next();
                if (workers.getSize() < maxNumberOfThreads && work.getData().running.get() > 0 && System.currentTimeMillis() - work.getData().lastFinish > maxWorkIdleTime) {
                    workers.add(new Worker());
                }

                workers.next();
                key.runnable.run();
                synchronized (key) {
                    key.lastFinish = System.currentTimeMillis();
                }
                key.running.incrementAndGet();
                lastRun = System.currentTimeMillis();
            }
        }
    }

    public QueueKey queue(Runnable runnable, int concurrentInstances) {
        if (workers.getSize() == 0) {
            workers.add(new Worker());
        }

        QueueKey queueKey = new QueueKey(runnable, concurrentInstances);
        work.add(queueKey);
        return queueKey;
    }

    public int getNumberOfThreads() {
        return workers.getSize();
    }

    @Override
    public String toString() {
        return name + "{" +
                "maxWorkIdleTime=" + maxWorkIdleTime +
                ", maxThreadIdleTime=" + maxThreadIdleTime +
                ", maxNumberOfThreads=" + maxNumberOfThreads +
                '}';
    }
}
