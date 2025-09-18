package de.yoyosource.streamable.internal.finish;

import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.internal.Element;

import java.util.concurrent.atomic.AtomicReference;

public class GreedySequentialFinish extends SequentialFinish {

    public GreedySequentialFinish(StreamableCollector collector) {
        super(collector);
    }

    @Override
    protected void processElement(Element element) {
        if (!containerInitialized) {
            container = collector.container();
            containerInitialized = true;
        }

        if (element instanceof Element.Value<?> value) {
            try {
                collector.accumulate(container, value.index(), value.value());
            } catch (Throwable e) {
                queueKey.dequeue();
                root.setError(e);
                finished = true;
            }
        } else {
            queueKey.dequeue();
            try {
                Object result = collector.finish(container);
                this.result = new AtomicReference<>(result);
            } catch (Throwable e) {
                root.setError(e);
                this.result = new AtomicReference<>(null);
            } finally {
                finished = true;
            }
        }
    }
}
