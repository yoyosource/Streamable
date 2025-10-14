package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.internal.Element;
import de.yoyosource.streamable.internal.Evaluator;
import de.yoyosource.streamable.internal.FinishException;
import de.yoyosource.streamable.internal.sequence.Sequence;
import de.yoyosource.streamable.internal.sequence.UnorderedSequence;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class FlattenStep extends Step implements Evaluator {

    private volatile boolean finished = false;
    private final AtomicLong index = new AtomicLong();
    private Sequence<Element<?>> elements = new UnorderedSequence<>();

    public FlattenStep() {
        super(null);
    }

    @Override
    public Ordering ordering() {
        return Ordering.ORDERED;
    }

    @Override
    public Set<Evaluation> evaluation() {
        return Evaluation.sequential_greedy;
    }

    @Override
    public void consume(long index, Object value) {
        if (finished) throw FinishException.INSTANCE;
        elements.inserter().add(new Element.Value<>(index, ((Iterable) value).iterator()));
    }

    @Override
    public void finish() {
        if (finished) throw FinishException.INSTANCE;
        elements.inserter().add(Element.Finish.getInstance());
    }

    @Override
    public boolean evaluateNext() {
        if (finished) return false;
        if (elements.isEmpty()) return false;
        Element element = elements.peek();

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
