package de.yoyosource.streamable.internal.root;

import de.yoyosource.streamable.internal.Evaluator;
import de.yoyosource.streamable.internal.FinishException;
import de.yoyosource.streamable.internal.StreamableSupplier;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

public class Root extends StreamableSupplier implements Evaluator {

    private long index = 0;
    private Iterator iterator;
    private boolean finished = false;

    @Setter
    @Getter
    private Throwable error = null;

    public Root(Iterator iterator) {
        this.iterator = iterator;
        this.root = this;
    }

    @Override
    public boolean evaluateNext() {
        if (finished) return false;
        try {
            if (iterator.hasNext()) {
                Object current = iterator.next();
                next.consume(index++, current);
                return true;
            } else {
                next.finish();
                return false;
            }
        } catch (FinishException e) {
            finished = true;
            return false;
        }
    }

    @Override
    public int backlogSize() {
        return 0;
    }
}
