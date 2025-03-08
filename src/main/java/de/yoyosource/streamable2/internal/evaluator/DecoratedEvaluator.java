package de.yoyosource.streamable2.internal.evaluator;

import de.yoyosource.streamable2.StreamableCollector;
import de.yoyosource.streamable2.StreamableGatherer;
import de.yoyosource.streamable2.internal.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public abstract class DecoratedEvaluator extends Evaluator {

    protected Evaluator source;

    protected DecoratedEvaluator(Evaluator source, AtomicLong layer) {
        super(layer);
        this.source = source;
    }

    protected List<Pair<StreamableGatherer, Boolean>> gatherers = new ArrayList<>();

    public void add(StreamableGatherer gatherer, boolean flatten) {
        layer.incrementAndGet();
        gatherers.add(new Pair<>(gatherer, flatten));
    }

    public abstract Object collect(StreamableCollector collector);

    @Override
    public DecoratedEvaluator sequential() {
        if (gatherers.isEmpty()) {
            return source.sequential();
        } else {
            return new SequentialEvaluator(this, layer);
        }
    }

    @Override
    public DecoratedEvaluator parallel() {
        if (gatherers.isEmpty()) {
            return source.parallel();
        } else {
            return new ParallelEvaluator(this, layer);
        }
    }

    @Override
    public long getLayer() {
        return gatherers.size() + source.getLayer();
    }
}
