package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.Ordering;
import de.yoyosource.streamable.internal.Element;
import de.yoyosource.streamable.internal.Evaluator;
import de.yoyosource.streamable.internal.FinishException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public class FlattenStep extends Step implements Evaluator {

    private volatile boolean finished = false;
    private final AtomicLong index = new AtomicLong();
    private Queue<Element<?>> elements = new LinkedList<>();

    public FlattenStep() {
        super(null);
    }

    @Override
    public Ordering ordering() {
        return Ordering.SEQUENTIAL;
    }

    @Override
    public void consume(long index, Object value) {
        if (finished) throw new FinishException();
        elements.add(new Element.Value<>(index, ((Iterable) value).iterator()));
    }

    @Override
    public void finish() {
        if (finished) throw new FinishException();
        elements.add(new Element.Finish());
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
            try {
                next.finish();
            } catch (FinishException e) {
                finished = true;
            }
            return false;
        }
    }

    @Override
    public int backlogSize() {
        return elements.size();
    }
}
