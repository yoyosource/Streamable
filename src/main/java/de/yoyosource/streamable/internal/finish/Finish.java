package de.yoyosource.streamable.internal.finish;

import de.yoyosource.streamable.StreamableCollector;
import de.yoyosource.streamable.internal.StreamableConsumer;
import de.yoyosource.streamable.internal.root.Root;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Finish implements StreamableConsumer {

    protected volatile Root root = null;

    @Getter
    protected final StreamableCollector collector;

    @Getter
    protected volatile AtomicReference<Object> result = null;

    protected Finish(StreamableCollector collector) {
        this.collector = collector;
    }
}
