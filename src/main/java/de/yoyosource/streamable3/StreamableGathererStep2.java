package de.yoyosource.streamable3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class StreamableGathererStep2 implements Step {

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(2000, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    // Finish/Short Circuit is missing!
    private final AtomicLong counter = new AtomicLong();
    private final StreamableGatherer gatherer;
    private final Sequence sequence = new Sequence();

    private Map<Thread, Object> containers = Collections.synchronizedMap(new HashMap<>());

    private volatile Step next = Noop.INSTANCE;

    public StreamableGathererStep2(StreamableGatherer streamableGatherer) {
        this.gatherer = streamableGatherer;
    }

    public <T extends Step> T setNext(T next) {
        if (next == null) {
            this.next = Noop.INSTANCE;
        } else {
            this.next = next;
        }
        return next;
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
            if (gatherer.integrate(container, value, o -> {
                // System.out.println("Data: " + o);
                inserter.add(o);
            })) {
                consume(new Element.Finish());
            }
            // System.out.println("Finish: " + value);
        } catch (Throwable e) {
            consume(new Element.Finish());
        } finally {
            inserter.release();
        }

        synchronized (sequence) {
            for (Object o : sequence) {
                // System.out.println(sequence.index() + " " + o);
                next.consume(new Element.Value<>(sequence.index(), o));
            }
        }
        counter.decrementAndGet();
    }

    private void finish() {
        while (!sequence.isEmpty()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Thread.yield();
        }
        /*
        while (counter.get() > 0) {
            Thread.yield();
        }
         */

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
        // System.out.println(current);

        Sequence.Inserter<Object> inserter = sequence.inserter();
        try {
            gatherer.finish(current, o -> {
                // System.out.println("Data: " + o);
                inserter.add(o);
            });
        } catch (Throwable e) {
            // Ignore
        } finally {
            inserter.release();
        }

        // System.out.println("Here");
        synchronized (sequence) {
            // System.out.println("Sequence runout");
            while (!sequence.isEmpty()) {
                for (Object o : sequence) {
                    next.consume(new Element.Value<>(sequence.index(), o));
                }
            }
            // System.out.println("Finish");
            next.consume(new Element.Finish());
        }
    }
}
