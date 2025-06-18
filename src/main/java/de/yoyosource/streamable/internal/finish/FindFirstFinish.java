package de.yoyosource.streamable.internal.finish;

import de.yoyosource.streamable.internal.Element;
import de.yoyosource.streamable.internal.FinishException;

import java.util.concurrent.atomic.AtomicReference;

public class FindFirstFinish extends Finish {

    private volatile boolean finished = false;

    public FindFirstFinish() {
        super(null);
    }

    @Override
    public void consume(Element element) {
        if (finished) throw new FinishException();
        finished = true;
        if (element instanceof Element.Value<?>) {
            this.result = new AtomicReference<>(((Element.Value<?>) element).value());
        } else {
            this.result = new AtomicReference<>(null);
        }
    }
}
