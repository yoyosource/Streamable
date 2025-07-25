package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.Ordering;
import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.ThreadManager;
import de.yoyosource.streamable.internal.FinishException;

import java.util.LinkedList;
import java.util.Queue;

public class SequentialStep extends Step {

    private final ThreadManager.QueueKey queueKey;
    private volatile long index = 0;
    private volatile boolean finished = false;

    private final Queue<Long> elementIndex = new LinkedList<>();
    private final Queue<Object> elementValue = new LinkedList<>();

    private boolean containerInitialized = false;
    private Object container = null;

    public SequentialStep(StreamableGatherer streamableGatherer) {
        super(streamableGatherer);
        queueKey = ThreadManager.queueToCurrent(() -> {
            if (finished) {
                processElement(null, null);
                return;
            }

            Long index;
            Object value;

            synchronized (elementIndex) {
                if (elementIndex.isEmpty() || elementValue.isEmpty()) return;
                index = elementIndex.poll();
                value = elementValue.poll();
            }

            processElement(index, value);
        }, 1);
    }

    @Override
    public Ordering ordering() {
        return gatherer.ordering();
    }

    @Override
    public void consume(long index, Object value) {
        if (finished) throw FinishException.INSTANCE;
        synchronized (elementIndex) {
            elementIndex.add(index);
            elementValue.add(value);
        }
    }

    @Override
    public void finish() {
        if (finished) throw FinishException.INSTANCE;
        synchronized (elementIndex) {
            elementIndex.add(null);
            elementValue.add(null);
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
            } catch (Throwable e) {
                queueKey.dequeue();
                root.setError(e);
                finished = true;
            }
        } else {
            queueKey.dequeue();
            finished = true;
            try {
                gatherer.finish(container, o -> {
                    next.consume(this.index++, o);
                });
                next.finish();
            } catch (FinishException e) {
                // Ignore
            } catch (Throwable e) {
                root.setError(e);
            }
        }
    }
}
