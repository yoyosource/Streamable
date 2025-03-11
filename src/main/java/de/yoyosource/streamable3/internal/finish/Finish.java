package de.yoyosource.streamable3.internal.finish;

import de.yoyosource.streamable3.StreamableCollector;
import de.yoyosource.streamable3.internal.StreamableConsumer;
import de.yoyosource.streamable3.internal.root.Root;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Finish implements StreamableConsumer {

    protected volatile Root root = null;
    protected final StreamableCollector collector;

    @Getter
    protected volatile AtomicReference<Object> result = null;

    protected Finish(StreamableCollector collector) {
        this.collector = collector;
    }
}
