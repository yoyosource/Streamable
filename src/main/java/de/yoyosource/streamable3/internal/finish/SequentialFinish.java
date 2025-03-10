package de.yoyosource.streamable3.internal.finish;

import de.yoyosource.streamable3.internal.Sequence;
import de.yoyosource.streamable3.StreamableCollector;
import de.yoyosource.streamable3.internal.Element;

import java.util.concurrent.atomic.AtomicReference;

public class SequentialFinish extends Finish {

    private volatile Thread thread = null;
    private volatile boolean finished = false;

    private final Sequence<Element> sequence = new Sequence<>();

    private boolean containerInitialized = false;
    private Object container = null;

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
                    if (sequence.hasNext()) {
                        processElement(sequence.next());
                    }
                }

                processElement(new Element.Finish());
            });
            thread.setDaemon(true);
            thread.start();
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
                    finished = true;
                }
            } catch (Throwable e) {
                finished = true;
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
