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
    public void consume(Element element) {
        if (element instanceof Element.Value<?> value) {
            elements.add(new Element.Value(value.index(), ((Iterable) value.value()).iterator()));
        } else {
            elements.add(element);
        }

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
                    next.consume(new Element.Value(index.getAndIncrement(), o));
                });
            } catch (FinishException e) {
                finished = true;
            } catch (Throwable e) {
                finished = true;
                next.consume(new Element.Finish());
            }
        } else {
            try {
                next.consume(element);
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
                next.consume(new Element.Value(index.getAndIncrement(), iterator.next()));
            } else {
                elements.remove();
            }
            return true;
        } else {
            next.consume(element);
            return false;
        }
    }
}
