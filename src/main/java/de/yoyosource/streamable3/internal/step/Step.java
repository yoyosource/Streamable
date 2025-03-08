package de.yoyosource.streamable3.internal.step;

import de.yoyosource.streamable3.StreamableGatherer;
import de.yoyosource.streamable3.internal.StreamableConsumer;
import de.yoyosource.streamable3.internal.StreamableSupplier;

public abstract class Step extends StreamableSupplier implements StreamableConsumer {

    protected final StreamableGatherer gatherer;

    protected Step(StreamableGatherer streamableGatherer) {
        this.gatherer = streamableGatherer;
    }
}
