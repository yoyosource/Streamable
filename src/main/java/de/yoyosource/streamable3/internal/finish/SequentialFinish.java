package de.yoyosource.streamable3.internal.finish;

import de.yoyosource.streamable3.Sequence;
import de.yoyosource.streamable3.StreamableCollector;
import de.yoyosource.streamable3.internal.Element;

import java.util.concurrent.atomic.AtomicReference;

public class SequentialFinish extends Finish {

    private final Sequence<Element> sequence = new Sequence<>();

    private volatile boolean finished = false;
    private boolean containerInitialized = false;
    private Object container = null;

    private volatile Thread thread = null;

    public SequentialFinish(StreamableCollector collector) {
        super(collector);
    }

    @Override
    public void consume(Element element) {
        if (finished) throw new IllegalStateException("This Stream Finish is already finished!");
        sequence.inserter().add(element).release();

        if (thread == null) {
            thread = new Thread(() -> {
                while (!finished) {
                    sequence.forEachRemaining(this::processElement);
                }
                sequence.forEachRemaining(this::processElement);
            });
            thread.setDaemon(true);
            thread.start();
        }

        if (element instanceof Element.Finish<?>) {
            finished = true;
        }
    }

    private void processElement(Element element) {
        if (!containerInitialized) {
            container = collector.container();
            containerInitialized = true;
        }

        if (element instanceof Element.Value<?> value) {
            try {
                if (collector.accumulate(container, value.index(), value.value())) {
                    consume(new Element.Finish());
                }
            } catch (Throwable e) {
                if (!finished) {
                    consume(new Element.Finish());
                }
            }
        } else {
            try {
                Object result = collector.finish(container);
                this.result = new AtomicReference<>(result);
            } catch (Throwable e) {
                this.result = new AtomicReference<>(null);
            }
        }
    }
}
