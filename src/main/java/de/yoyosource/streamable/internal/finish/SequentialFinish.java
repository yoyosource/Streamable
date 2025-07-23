package de.yoyosource.streamable.internal.finish;

import de.yoyosource.streamable.Ordering;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.ThreadManager;
import de.yoyosource.streamable.internal.Element;
import de.yoyosource.streamable.internal.FinishException;
import de.yoyosource.streamable.internal.OrderedSequence;

import java.util.concurrent.atomic.AtomicReference;

public class SequentialFinish extends Finish {

    private final ThreadManager.QueueKey queueKey;
    private volatile boolean finished = false;

    private final OrderedSequence<Element> sequence = new OrderedSequence<>();

    private boolean containerInitialized = false;
    private Object container = null;

    public SequentialFinish(StreamableCollector collector) {
        super(collector);
        queueKey = ThreadManager.queueToCurrent(() -> {
            if (sequence.hasNext()) {
                processElement(sequence.next());
            }
        }, 1);
    }

    @Override
    public Ordering ordering() {
        return collector.ordering();
    }

    @Override
    public void consume(Element element) {
        if (finished) throw new FinishException();
        sequence.inserter().add(element).release();
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
                queueKey.dequeue();
                root.setError(e);
                finished = true;
            }
        } else {
            queueKey.dequeue();
            try {
                Object result = collector.finish(container);
                collector.close();
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
