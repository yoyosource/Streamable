package de.yoyosource.streamable.internal;

import java.util.List;

public interface InternalStreamable {

    InternalStreamable setNext(StreamableConsumer streamableConsumer);
    <T> T evaluate();

    int getMaxParallelTasks();
    void setMaxParallelTasks(int maxParallelTasks);

    List<Runnable> getCloseHandlers();
    void addCloseHandler(List<Runnable> closeHandlers);
}
