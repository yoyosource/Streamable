package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.Ordering;
import de.yoyosource.streamable.internal.FinishException;

public class OnCloseStep extends Step {

    private Runnable closeHandler;
    private boolean finished = false;

    public OnCloseStep(Runnable closeHandler) {
        super(null);
        this.closeHandler = closeHandler;
    }

    @Override
    public Ordering ordering() {
        return Ordering.UNORDERED;
    }

    @Override
    public void consume(long index, Object value) {
        if (finished) {
            throw FinishException.INSTANCE;
        }
        try {
            next.consume(index, value);
        } catch (FinishException e) {
            closeHandler.run();
            finished = true;
            throw e;
        }
    }

    @Override
    public void finish() {
        if (!finished) {
            closeHandler.run();
            finished = true;
            next.finish();
        } else {
            throw FinishException.INSTANCE;
        }
    }
}
