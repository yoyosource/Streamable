package de.yoyosource.streamable.internal.step;

import de.yoyosource.streamable.StreamableGatherer;
import de.yoyosource.streamable.internal.StreamableConsumer;
import de.yoyosource.streamable.internal.StreamableSupplier;

public abstract class Step extends StreamableSupplier implements StreamableConsumer {

    protected final StreamableGatherer gatherer;

    protected Step(StreamableGatherer streamableGatherer) {
        this.gatherer = streamableGatherer;
    }
}
