package de.yoyosource.streamable2.internal.evaluator;

import de.yoyosource.streamable2.StreamableCollector;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;

public class SequentialEvaluator extends DecoratedEvaluator {

    public SequentialEvaluator(Evaluator source, AtomicLong layer) {
        super(source, layer);
    }

    @Override
    public Spliterator spliterator() {
        if (gatherers.isEmpty()) {
            return source.spliterator();
        }
        return null; // TODO: Implement
    }

    @Override
    public Object collect(StreamableCollector collector) {
        return null; // TODO: Implement
    }

    @Override
    public DecoratedEvaluator sequential() {
        return this;
    }
}
