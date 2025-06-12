package de.yoyosource.streamable.internal.finish;

import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.internal.Element;
import de.yoyosource.streamable.internal.FinishException;
import de.yoyosource.streamable.internal.Sequence;

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
        if (finished) throw new FinishException();
        sequence.inserter().add(element).release();

        if (thread == null) {
            thread = new Thread(() -> {
                while (!finished) {
                    if (sequence.hasNext()) {
                        processElement(sequence.next());
                    }
                }
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
                    processElement(new Element.Finish());
                }
            } catch (Throwable e) {
                root.setError(e);
                finished = true;
            }
        } else {
            try {
                Object result = collector.finish(container);
                collector.close();
                this.result = new AtomicReference<>(result);
                finished = true;
            } catch (Throwable e) {
                root.setError(e);
                this.result = new AtomicReference<>(null);
            }
        }
    }
}
