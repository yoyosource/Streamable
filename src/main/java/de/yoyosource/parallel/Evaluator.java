package de.yoyosource.parallel;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.impl.JavaStream;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Evaluator {

    private static final ExecutorService THREAD_POOL_EXECUTOR = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() + 2);

    private class Node {
        private Spliterator current = null;
        private Node next = null;
        private volatile boolean finished = false;
        private List list = new ArrayList();
        // Ignore further calculations!
    }

    private Node root;
    private AtomicLong counter = new AtomicLong();
    private AtomicInteger parallelism = new AtomicInteger();
    private int maxParallelism = 0;

    public Evaluator(Spliterator spliterator) {
        this.root = new Node();
        this.root.current = spliterator;
    }

    private void evaluate(Node node) {
        parallelism.incrementAndGet();
        if (maxParallelism < parallelism.get()) {
            maxParallelism = parallelism.get();
        }

        node.finished = false;
        long time = System.currentTimeMillis();
        while (node.current.tryAdvance(o -> {
            // System.out.println(o);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            node.list.add(o);
        })) {
            if (System.currentTimeMillis() - time < 50) continue;
            Spliterator current = node.current;
            Spliterator other = current.trySplit();
            if (other == null) continue;

            node.current = other;
            Node afterSplit = new Node();
            afterSplit.next = node.next;
            afterSplit.current = current;
            node.next = afterSplit;

            time = System.currentTimeMillis();
            THREAD_POOL_EXECUTOR.execute(() -> evaluate(afterSplit));
        }
        node.finished = true;
        counter.addAndGet(node.list.size());
        parallelism.decrementAndGet();
        // System.out.println(node.counter.get());
    }

    public long awaitFinish() {
        long count;
        while (true) {
            count = 0;
            Node current = this.root;
            while (current != null) {
                if (!current.finished) break;
                current = current.next;
                count++;
            }
            if (current == null) break;
        }
        return count;
    }

    public static void main(String[] args) throws Exception {
        Spliterator<BigInteger> spliterator = Streamable.iterate(BigInteger.ONE, bigInteger -> bigInteger.add(BigInteger.ONE))
                .as(JavaStream.type())
                // .limit(1_000_000_000)
                .limit(100_000_000)
                .spliterator();

        Evaluator evaluator = new Evaluator(spliterator);
        THREAD_POOL_EXECUTOR.execute(() -> evaluator.evaluate(evaluator.root));
        long countOfNodes = evaluator.awaitFinish();
        System.out.println(": " + evaluator.counter + " " + countOfNodes + " " + evaluator.maxParallelism);
    }
}
