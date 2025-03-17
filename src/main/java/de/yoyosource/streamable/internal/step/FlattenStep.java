package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.internal.Element;
import de.yoyosource.streamable.internal.Evaluators;
import de.yoyosource.streamable.internal.FinishException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public class FlattenStep extends Step implements Evaluators {

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
                ((Iterable<Object>) value.value()).forEach(o -> {
                    next.consume(index.getAndIncrement(), o);
                });
            } catch (FinishException e) {
                finished = true;
            } catch (Throwable e) {
                finished = true;
                next.finish();
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
        Element element = elements.peek();
        if (element == null) return false;

        if (element instanceof Element.Value<?> value) {
            Iterator<Object> iterator = ((Iterator<Object>) value.value());
            if (iterator.hasNext()) {
                next.consume(index.getAndIncrement(), iterator.next());
            } else {
                elements.remove();
            }
            return true;
        } else {
            next.finish();
            return false;
        }
    }
}
