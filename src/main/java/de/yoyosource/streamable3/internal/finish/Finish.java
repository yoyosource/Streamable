package de.yoyosource.streamable3.internal.finish;

import de.yoyosource.streamable3.StreamableCollector;
import de.yoyosource.streamable3.internal.StreamableConsumer;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Finish implements StreamableConsumer {

    protected final StreamableCollector collector;
    protected volatile AtomicReference<Object> result = null;

    protected Finish(StreamableCollector collector) {
        this.collector = collector;
    }

    public Object waitForResult() {
        while (result == null) {
            Thread.yield();
        }
        return result.get();
    }
}
