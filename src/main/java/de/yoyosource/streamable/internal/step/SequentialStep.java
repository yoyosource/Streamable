package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.FinishException;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SequentialStep extends Step {

    private volatile Thread thread = null;
    private volatile long index = 0;
    private volatile boolean finished = false;

    private final Queue<Long> elementIndex = new LinkedList<>();
    private final Queue<Object> elementValue = new LinkedList<>();

    private boolean containerInitialized = false;
    private Object container = null;

    public SequentialStep(StreamableGatherer streamableGatherer) {
        super(streamableGatherer);
    }

    @Override
    public void consume(long index, Object value) {
        if (finished) throw new FinishException();
        synchronized (elementIndex) {
            elementIndex.add(index);
            elementValue.add(value);
        }
        startThread();
    }

    @Override
    public void finish() {
        if (finished) throw new FinishException();
        synchronized (elementIndex) {
            elementIndex.add(null);
            elementValue.add(null);
        }
        startThread();
    }

    private void startThread() {
        if (thread == null) {
            thread = new Thread(() -> {
                while (!finished) {
                    Long index;
                    Object value;

                    synchronized (elementIndex) {
                        if (elementIndex.isEmpty() || elementValue.isEmpty()) continue;
                        index = elementIndex.poll();
                        value = elementValue.poll();
                    }

                    processElement(index, value);
                }

                processElement(null, null);
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void processElement(Long index, Object value) {
        if (!containerInitialized) {
            container = gatherer.container();
            containerInitialized = true;
        }

        if (index != null) {
            try {
                if (gatherer.integrate(container, index, value, o -> {
                    next.consume(this.index++, o);
                })) {
                    finished = true;
                }
            } catch (FinishException e) {
                finished = true;
            }
        } else {
            try {
                gatherer.finish(container, o -> {
                    next.consume(this.index++, o);
                });
                gatherer.close();
                next.finish();
            } catch (FinishException e) {
                // Ignore
            }
        }
    }
}
