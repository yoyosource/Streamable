package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.Ordering;
import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.internal.Element;
import de.yoyosource.streamable.internal.Evaluator;
import de.yoyosource.streamable.internal.FinishException;
import de.yoyosource.streamable.streams.ZippedStream;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public class ZipStep extends Step implements Evaluator {

    private Iterator<Object> iterator;
    private boolean ignoreNulls;

    private volatile boolean finished = false;
    private final AtomicLong index = new AtomicLong();
    private Queue<Element<?>> elements = new LinkedList<>();

    public ZipStep(Streamable streamable, boolean ignoreNulls) {
        super(null);
        iterator = streamable.iterator();
        this.ignoreNulls = ignoreNulls;
    }

    @Override
    public Ordering ordering() {
        return Ordering.SEQUENTIAL;
    }

    @Override
    public void consume(long index, Object value) {
        if (finished) throw new FinishException();
        elements.add(new Element.Value<>(index, value));
    }

    @Override
    public void finish() {
        if (finished) throw new FinishException();
        elements.add(new Element.Finish());
    }

    @Override
    public boolean evaluateNext() {
        if (finished) {
            if (ignoreNulls) {
                return false;
            }
            if (iterator.hasNext()) {
                Object value = iterator.next();
                next.consume(index.getAndIncrement(), new ZippedStream.Zip<>(null, value));
            } else {
                try {
                    next.finish();
                } catch (FinishException e) {
                    finished = true;
                }
            }
            return iterator.hasNext();
        }

        Element element = elements.poll();
        if (element == null) return iterator.hasNext();

        if (element instanceof Element.Value<?> value) {
            if (iterator.hasNext()) {
                Object other = iterator.next();
                next.consume(index.getAndIncrement(), new ZippedStream.Zip<>(value.value(), other));
            } else {
                if (ignoreNulls) return false;
                next.consume(index.getAndIncrement(), new ZippedStream.Zip<>(value.value(), null));
            }
            return true;
        } else {
            finished = true;
            if (!ignoreNulls && iterator.hasNext()) {
                return true;
            }
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
