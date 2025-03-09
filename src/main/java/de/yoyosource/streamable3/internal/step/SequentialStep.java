package de.yoyosource.streamable3.internal.step;

import de.yoyosource.streamable3.Sequence;
import de.yoyosource.streamable3.StreamableGatherer;
import de.yoyosource.streamable3.internal.Element;

public class SequentialStep extends Step {

    private volatile Thread thread = null;
    private volatile long index = 0;
    private volatile boolean finished = false;

    private final Sequence<Element> sequence = new Sequence<>();

    private boolean containerInitialized = false;
    private Object container = null;

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
            container = gatherer.container();
            containerInitialized = true;
        }

        if (element instanceof Element.Value<?> value) {
            try {
                if (gatherer.integrate(container, value.index(), value.value(), o -> {
                    next.consume(new Element.Value<>(index++, o));
                })) {
                    finished = true;
                }
            } catch (Throwable e) {
                finished = true;
            }
        } else {
            try {
                gatherer.finish(container, o -> {
                    next.consume(new Element.Value(index++, o));
                });
            } catch (Throwable e) {
                // Ignore
            }
            next.consume(new Element.Finish());
        }
    }
}
