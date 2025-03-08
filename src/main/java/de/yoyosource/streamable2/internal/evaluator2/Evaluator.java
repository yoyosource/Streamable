package de.yoyosource.streamable2.internal.evaluator2;

import de.yoyosource.streamable2.StreamableGatherer;
import de.yoyosource.streamable2.internal.iter.Iter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class Evaluator {

    @Getter
    private Evaluator previous;

    @Getter
    private Evaluator next;

    public Evaluator add(Evaluator next) {
        this.next = next;
        next.previous = this;
        return next;
    }

    protected final StreamableGatherer streamableGatherer;
    protected final boolean flatten;

    protected Evaluator(StreamableGatherer streamableGatherer, boolean flatten) {
        this.streamableGatherer = streamableGatherer;
        this.flatten = flatten;
    }

    protected final List<Iter> iters = new ArrayList<>();

    public boolean hasIter() {
        iters.removeIf(Predicate.not(Iter::hasNext));
        return !iters.isEmpty();
    }

    public abstract boolean apply(Object o, boolean runFinished);
}
