package de.yoyosource.streamable2.internal.evaluator;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;

public class SourceEvaluator extends Evaluator {

    private Spliterator source;

    public SourceEvaluator(Spliterator source) {
        super(new AtomicLong());
        this.source = source;
    }

    @Override
    public Spliterator spliterator() {
        return source;
    }

    @Override
    public DecoratedEvaluator sequential() {
        return new SequentialEvaluator(this, layer);
    }

    @Override
    public DecoratedEvaluator parallel() {
        return new ParallelEvaluator(this, layer);
    }

    @Override
    public long getLayer() {
        return 0;
    }
}
