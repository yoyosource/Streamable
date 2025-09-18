package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.FinishException;

public class GreedySequentialStep extends SequentialStep {

    public GreedySequentialStep(StreamableGatherer streamableGatherer) {
        super(streamableGatherer);
    }

    @Override
    protected void processElement(Long index, Object value) {
        if (!containerInitialized) {
            container = gatherer.container();
            containerInitialized = true;
        }

        if (index != null) {
            try {
                gatherer.integrate(container, index, value, nextSink);
            } catch (FinishException e) {
                finished = true;
            } catch (Throwable e) {
                queueKey.dequeue();
                root.setError(e);
                finished = true;
            }
        } else {
            queueKey.dequeue();
            finished = true;
            try {
                gatherer.finish(container, o -> {
                    next.consume(this.index++, o);
                });
                next.finish();
            } catch (FinishException e) {
                // Ignore
            } catch (Throwable e) {
                root.setError(e);
            }
        }
    }
}
