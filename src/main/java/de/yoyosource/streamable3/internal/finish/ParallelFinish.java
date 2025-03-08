package de.yoyosource.streamable3.internal.finish;

import de.yoyosource.streamable3.StreamableCollector;
import de.yoyosource.streamable3.internal.Element;

public class ParallelFinish extends Finish {

    public ParallelFinish(StreamableCollector collector, int maxParallelTasks) {
        super(collector);
        System.out.println(maxParallelTasks);
    }

    @Override
    public void consume(Element element) {

    }
}
