package de.yoyosource.streamable.internal.root;

import de.yoyosource.streamable.internal.Evaluators;
import de.yoyosource.streamable.internal.FinishException;
import de.yoyosource.streamable.internal.StreamableSupplier;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

public class Root extends StreamableSupplier implements Evaluators {

    private long index = 0;
    private Iterator iterator;

    @Setter
    @Getter
    private Throwable error = null;

    public Root(Iterator iterator) {
        this.iterator = iterator;
        this.root = this;
    }

    public void evaluate() {
        try {
            iterator.forEachRemaining(o -> {
                next.consume(index++, o);
            });
            next.finish();
        } catch (FinishException e) {
            // Ignore
        }
    }

    @Override
    public boolean evaluateNext() {
        if (iterator.hasNext()) {
            next.consume(index++, iterator.next());
            return true;
        } else {
            next.finish();
            return false;
        }
    }
}
