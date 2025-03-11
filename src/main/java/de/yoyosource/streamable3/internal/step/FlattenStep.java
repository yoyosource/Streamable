package de.yoyosource.streamable3.internal.step;

import de.yoyosource.streamable3.internal.Element;
import de.yoyosource.streamable3.internal.FinishException;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public class FlattenStep extends Step {

    private volatile Thread thread = null;
    private volatile boolean finished = false;
    private final AtomicLong index = new AtomicLong();
    private Queue<Element<?>> elements = new LinkedList<>();

    public FlattenStep() {
        super(null);
    }

    @Override
    public void consume(Element element) {
        elements.add(element);

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
}
