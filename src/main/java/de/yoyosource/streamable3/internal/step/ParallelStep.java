package de.yoyosource.streamable3.internal.step;

import de.yoyosource.streamable3.Sequence;
import de.yoyosource.streamable3.StreamableGatherer;
import de.yoyosource.streamable3.internal.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class ParallelStep extends Step {

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(2000, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    // Finish/Short Circuit is missing!
    private final AtomicLong counter = new AtomicLong();
    private final Sequence sequence = new Sequence();

    private Map<Thread, Object> containers = Collections.synchronizedMap(new HashMap<>());

    public ParallelStep(StreamableGatherer streamableGatherer, int maxParallelTasks) {
        super(streamableGatherer);
    }

    @Override
    public synchronized void consume(Element element) {
        if (element instanceof Element.Value<?> value) {
            counter.incrementAndGet();
            EXECUTOR.execute(() -> value(value, sequence.inserter()));
        } else if (element instanceof Element.Finish<?>) {
            EXECUTOR.execute(() -> finish());
        }
    }

    private void value(Element.Value<?> value, Sequence.Inserter<Object> inserter) {
        Object container = containers.computeIfAbsent(Thread.currentThread(), thread -> gatherer.container());
        try {
            if (gatherer.integrate(container, value.index(), value.value(), o -> {
                inserter.add(o);
            })) {
                consume(new Element.Finish());
            }
        } catch (Throwable e) {
            consume(new Element.Finish());
        } finally {
            inserter.release();
        }

        synchronized (sequence) {
            for (Object o : sequence) {
                next.consume(new Element.Value<>(sequence.index(), o));
            }
        }
        counter.decrementAndGet();
    }

    private void finish() {
        while (counter.get() > 0) {
            Thread.yield();
        }

        Iterator<Object> objects = containers.values().iterator();
        Object current = null;
        for (long i = 0; true; i++) {
            if (!objects.hasNext()) {
                if (i == 0) {
                    next.consume(new Element.Finish());
                    return;
                } else {
                    break;
                }
            }

            Object next = objects.next();
            if (i == 0) {
                current = next;
            } else {
                current = gatherer.combine(current, next);
            }
        }

        Sequence.Inserter<Object> inserter = sequence.inserter();
        try {
            gatherer.finish(current, o -> {
                inserter.add(o);
            });
        } catch (Throwable e) {
            // Ignore
        } finally {
            inserter.release();
        }

        synchronized (sequence) {
            while (!sequence.isEmpty()) {
                for (Object o : sequence) {
                    next.consume(new Element.Value<>(sequence.index(), o));
                }
            }
            next.consume(new Element.Finish());
        }
    }
}
