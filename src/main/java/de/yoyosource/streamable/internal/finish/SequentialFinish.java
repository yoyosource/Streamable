package de.yoyosource.streamable.internal.finish;

import de.yoyosource.streamable.Evaluation;
import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.ThreadManager;
import de.yoyosource.streamable.internal.Element;
import de.yoyosource.streamable.internal.FinishException;
import de.yoyosource.streamable.internal.sequence.OrderedSequence;

import java.util.concurrent.atomic.AtomicReference;

public class SequentialFinish extends Finish {

    private final ThreadManager.QueueKey queueKey;
    private volatile boolean finished = false;

    private final OrderedSequence<Element> sequence = new OrderedSequence<>();

    private boolean containerInitialized = false;
    private Object container = null;

    private final boolean greedy;

    public SequentialFinish(StreamableCollector collector) {
        super(collector);
        queueKey = ThreadManager.queueToCurrent(() -> {
            if (sequence.hasNext()) {
                processElement(sequence.next());
            }
        }, 1);
        this.greedy = StreamableCollector.getEvaluation(collector).contains(Evaluation.GREEDY);
    }

    @Override
    public Evaluation.EvaluationValidSet evaluation() {
        return StreamableCollector.getEvaluation(collector);
    }

    @Override
    public void consume(Element element) {
        if (finished) throw FinishException.INSTANCE;
        sequence.inserter().add(element).release();
    }

    private void processElement(Element element) {
        if (!containerInitialized) {
            container = collector.container();
            containerInitialized = true;
        }

        if (element instanceof Element.Value<?> value) {
            try {
                if (greedy) {
                    collector.accumulate(container, value.index(), value.value());
                } else {
                    if (!collector.accumulate(container, value.index(), value.value())) {
                        finished = true;
                        processElement(Element.Finish.getInstance());
                    }
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
