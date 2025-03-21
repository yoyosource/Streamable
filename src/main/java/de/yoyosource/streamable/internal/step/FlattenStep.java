package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.internal.Element;
import de.yoyosource.streamable.internal.Evaluator;
import de.yoyosource.streamable.internal.FinishException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public class FlattenStep extends Step implements Evaluator {

    private volatile Thread thread = null;
    private volatile boolean finished = false;
    private final AtomicLong index = new AtomicLong();
    private Queue<Element<?>> elements = new LinkedList<>();

    public FlattenStep() {
        super(null);
    }

    @Override
    public void consume(long index, Object value) {
        elements.add(new Element.Value<>(index, ((Iterable) value).iterator()));
        startThread();
    }

    @Override
    public void finish() {
        elements.add(new Element.Finish());
        startThread();
    }

    private void startThread() {
        if (thread == null) {
            thread = new Thread(() -> {
                while (!finished) {
                    while (!elements.isEmpty()) {
                        processElement(elements.poll());
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void processElement(Element element) {
        if (element instanceof Element.Value<?> value) {
            try {
                ((Iterator<Object>) value.value()).forEachRemaining(o -> {
                    next.consume(index.getAndIncrement(), o);
                });
            } catch (FinishException e) {
                finished = true;
            } catch (Throwable e) {
                finished = true;
                next.finish();
                root.setError(e);
            }
        } else {
            try {
                next.finish();
            } catch (FinishException e) {
                // Ignore
            }
        }
    }

    @Override
    public boolean evaluateNext() {
        if (finished) return false;
        Element element = elements.peek();
        if (element == null) return false;

        if (element instanceof Element.Value<?> value) {
            Iterator<Object> iterator = ((Iterator<Object>) value.value());
            if (iterator.hasNext()) {
                try {
                    next.consume(index.getAndIncrement(), iterator.next());
                } catch (FinishException e) {
                    finished = true;
                    return false;
                }
            } else {
                elements.remove();
            }
            return true;
        } else {
            next.finish();
            return false;
        }
    }

    @Override
    public int backlogSize() {
        return elements.size();
    }
}
