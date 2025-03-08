package de.yoyosource.streamable3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class StreamableGathererStep implements Step {

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(1000, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    private StreamableGatherer gatherer;
    private long maxActiveTasks;

    private final AtomicLong incompleteTasks = new AtomicLong();
    private final AtomicLong activeTasks = new AtomicLong();
    private Map<Thread, Object> containers = Collections.synchronizedMap(new HashMap<>());
    private AtomicBoolean finished = new AtomicBoolean(false);
    private AtomicLong maxIndex = new AtomicLong();

    private volatile Step next = Noop.INSTANCE;

    public StreamableGathererStep(StreamableGatherer gatherer, long maxActiveTasks) {
        this.gatherer = gatherer;
        this.maxActiveTasks = maxActiveTasks;
    }

    public void setNext(Step next) {
        if (next == null) {
            this.next = Noop.INSTANCE;
        } else {
            this.next = next;
        }
    }

    public synchronized void consume(Element element) {
        if (finished.get()) return;
        if (element instanceof Element.Value<?> value) {
            incompleteTasks.incrementAndGet();
            EXECUTOR.execute(() -> run(value));
        }
        if (element instanceof Element.Finish<?>) {
            EXECUTOR.execute(this::finish);
        }
    }

    private void run(Element.Value<?> value) {
        synchronized (activeTasks) {
            if (maxActiveTasks > 0 && activeTasks.get() >= maxActiveTasks) {
                EXECUTOR.execute(() -> run(value));
                return;
            }
            activeTasks.incrementAndGet();
        }

        Object container = containers.computeIfAbsent(Thread.currentThread(), thread -> gatherer.container());
        if (gatherer.integrate(container, value, o -> {
            if (finished.get()) return;
            Element next = new Element.Value(value.index(), o);
            maxIndex.accumulateAndGet(value.index() + 1, Math::max);
            this.next.consume(next);
        }) && !finished.getAndSet(true)) {
            consume(new Element.Finish());
        }

        synchronized (activeTasks) {
            activeTasks.decrementAndGet();
        }
        incompleteTasks.decrementAndGet();
    }

    private void finish() {
        synchronized (activeTasks) {
            if (activeTasks.get() != 0 || incompleteTasks.get() != 0) {
                EXECUTOR.execute(this::finish);
                return;
            }
        }

        finished.set(true);

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

        gatherer.finish(current, o -> {
            System.out.println(o);
            next.consume(new Element.Value<>(maxIndex.getAndIncrement(), o));
        });
        next.consume(new Element.Finish());
    }
}
