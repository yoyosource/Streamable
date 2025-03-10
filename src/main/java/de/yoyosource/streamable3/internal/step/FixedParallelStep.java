package de.yoyosource.streamable3.internal.step;

import de.yoyosource.streamable3.Sequence;
import de.yoyosource.streamable3.StreamableGatherer;
import de.yoyosource.streamable3.internal.Element;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FixedParallelStep extends Step {

    private int maxParallelTasks;
    private List<Thread> threads = new ArrayList<>();
    private final Queue<Element.Value<Runnable>> queue = new LinkedList<>();

    private long finish = Integer.MAX_VALUE;
    private long counter = 0;

    private Sequence containers = new Sequence();
    private Sequence results = new Sequence();

    public FixedParallelStep(StreamableGatherer streamableGatherer, int maxParallelTasks) {
        super(streamableGatherer);
        if (maxParallelTasks <= 1) {
            this.maxParallelTasks = Integer.MAX_VALUE;
        } else {
            this.maxParallelTasks = maxParallelTasks;
        }
    }

    @Override
    public void consume(Element element) {
        if (counter > finish) throw new IllegalStateException("This Stream Step is already finished!");

        if (element instanceof Element.Value<?> value) {
            Sequence.Inserter container = containers.inserter();
            Sequence.Inserter result = results.inserter();
            synchronized (queue) {
                queue.add(new Element.Value<>(counter++, () -> processValue(value, container, result)));
            }
        } else if (element instanceof Element.Finish<?> finish) {
            synchronized (queue) {
                this.finish = Math.min(this.finish, counter);
                queue.add(new Element.Value<>(counter++, () -> processFinish(finish)));
            }
        }

        if (threads.size() < maxParallelTasks && queue.size() > threads.size() * threads.size() * 10) {
            new WorkerThread();
        }
    }

    private class WorkerThread extends Thread {

        public WorkerThread() {
            setDaemon(true);
            threads.add(this);
            start();
        }

        @Override
        public void run() {
            while (true) {
                Element.Value<Runnable> value;
                synchronized (queue) {
                    if (queue.isEmpty()) continue;
                    value = queue.poll();
                }

                if (value.index() > finish) continue;
                value.value().run();
                // System.out.println("Finished: " + value.index());
            }

            // We might wanna stop the threads if for long periods no work is present!
            // threads.remove(this);
        }
    }

    private void processValue(Element.Value<?> element, Sequence.Inserter container, Sequence.Inserter result) {
        System.out.println("Process Value: " + element);
        // TODO: Implement
    }

    private void processFinish(Element.Finish finish) {
        System.out.println("Process Finish: " + finish);
        System.out.println("Number of final Threads: " + threads.size());
        // TODO: Implement
    }
}
