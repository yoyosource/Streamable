package de.yoyosource.parallel;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.impl.JavaStream;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class SpliteratorExecutor {

    private static final ThreadGroup threadGroup = new ThreadGroup("SpliteratorExecutor");

    private static final int maxThread = Runtime.getRuntime().availableProcessors() + 2;

    private static final Set<ManagedThread> activeThreads = Collections.synchronizedSet(new HashSet<>());
    private static final Queue<ManagedThread> threadQueue = new ConcurrentLinkedQueue<>();
    private static final Queue<Spliterator<BigInteger>> spliteratorQueue = new ConcurrentLinkedQueue<>();

    static {
        Thread thread = new Thread(threadGroup, () -> {
            while (true) {
                if (spliteratorQueue.isEmpty()) continue;
                if (activeThreads.size() + threadQueue.size() < maxThread && threadQueue.isEmpty()) {
                    ManagedThread managedThread = new ManagedThread(threadGroup, threadGroup.getName() + "-" + threadGroup.activeCount());
                    System.out.println(System.currentTimeMillis() + ": Create managed " + managedThread);
                    threadQueue.add(managedThread);
                }
                if (!threadQueue.isEmpty()) {
                    ManagedThread managedThread = threadQueue.remove();
                    System.out.println(System.currentTimeMillis() + ": Assign work to " + managedThread);
                    activeThreads.add(managedThread);
                    managedThread.setWork(spliteratorQueue.remove());
                }
            }
        });
        thread.setName("Manager");
        thread.setDaemon(true);
        thread.start();
    }

    private static class ManagedThread extends Thread {

        public ManagedThread(ThreadGroup group, String name) {
            super(group, name);
        }

        private final Object lock = new Object();
        private volatile Spliterator<BigInteger> spliterator;

        private void setWork(Spliterator<BigInteger> spliterator) {
            this.spliterator = spliterator;
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        {
            setDaemon(true);
            start();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (lock) {
                        lock.wait(100);
                    }
                } catch (InterruptedException e) {
                    // Ignore
                }
                if (spliterator == null) continue;

                System.out.println(System.currentTimeMillis() + ": " + this + " is working on " + spliterator);
                long time = System.currentTimeMillis();
                AtomicLong count = new AtomicLong();
                AtomicReference<BigInteger> reference = new AtomicReference<>();
                while (spliterator.tryAdvance(o -> {
                    count.incrementAndGet();
                    if (reference.get() == null) {
                        reference.set(o);
                    } else {
                        reference.set(reference.get().multiply(o));
                    }
                })) {
                    if (System.currentTimeMillis() - time < 50) continue;
                    if (spliterator.estimateSize() < 10_000) continue;
                    if (activeThreads.size() >= maxThread) continue;
                    Spliterator<BigInteger> other = spliterator.trySplit();
                    System.out.println(System.currentTimeMillis() + ": Split by " + this + " of " + spliterator + " into " + other);
                    time = System.currentTimeMillis();
                    if (other == null) continue;
                    spliteratorQueue.add(other);
                }
                spliterator = null;

                System.out.println(System.currentTimeMillis() + ": deactivate " + this + " after seeing " + count.get() + " elements");
                activeThreads.remove(this);
                threadQueue.add(this);
            }
        }
    }

    public static void submit(Spliterator<BigInteger> spliterator) {
        spliteratorQueue.add(spliterator);
    }

    public static void main(String[] args) throws Exception {
        Spliterator<BigInteger> spliterator = Streamable.iterate(BigInteger.ONE, bigInteger -> bigInteger.add(BigInteger.ONE))
                .as(JavaStream.type())
                .limit(100_000_000)
                .spliterator();
        submit(spliterator);

        Thread.sleep(1_000);

        while (!activeThreads.isEmpty()) {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println(activeThreads.size() + ": " + activeThreads);
        System.out.println(threadQueue.size() + ": " + threadQueue);
        System.out.println(spliteratorQueue.size() + ": " + spliteratorQueue);
    }
}
