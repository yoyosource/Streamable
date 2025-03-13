package de.yoyosource.streamable.internal;

public interface InternalStreamable {

    InternalStreamable setNext(StreamableConsumer streamableConsumer);
    <T> T evaluate();

    int getMaxParallelTasks();
    void setMaxParallelTasks(int maxParallelTasks);
}
