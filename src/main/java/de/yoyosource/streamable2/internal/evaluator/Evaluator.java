package de.yoyosource.streamable2.internal.evaluator;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Evaluator {

    protected AtomicLong layer;

    protected Evaluator(AtomicLong layer) {
        this.layer = layer;
    }

    public abstract Spliterator spliterator();

    public abstract DecoratedEvaluator sequential();
    public abstract DecoratedEvaluator parallel();

    public long getLayer() {
        return layer.get();
    }
}
