package de.yoyosource.streamable3.internal.step;

import de.yoyosource.streamable3.Sequence;
import de.yoyosource.streamable3.StreamableGatherer;
import de.yoyosource.streamable3.internal.Element;

import java.util.concurrent.atomic.AtomicLong;

public class SequentialStep extends Step {

    private final AtomicLong index = new AtomicLong();
    private final Sequence<Element> sequence = new Sequence<>();

    private volatile boolean finished = false;
    private boolean containerInitialized = false;
    private Object container = null;

    private volatile Thread thread = null;

    public SequentialStep(StreamableGatherer streamableGatherer) {
        super(streamableGatherer);
    }

    @Override
    public synchronized void consume(Element element) {
        if (finished) throw new IllegalStateException("This Stream Step is already finished!");
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
            container = gatherer.container();
            containerInitialized = true;
        }

        if (element instanceof Element.Value<?> value) {
            try {
                if (gatherer.integrate(container, value.index(), value.value(), o -> {
                    next.consume(new Element.Value<>(index.getAndIncrement(), o));
                })) {
                    consume(new Element.Finish());
                }
            } catch (Throwable e) {
                if (!finished) {
                    consume(new Element.Finish());
                }
            }
        } else {
            try {
                gatherer.finish(container, o -> {
                    next.consume(new Element.Value(index.getAndIncrement(), o));
                });
            } catch (Throwable e) {
                // Ignore
            }
            next.consume(new Element.Finish());
        }
    }
}
