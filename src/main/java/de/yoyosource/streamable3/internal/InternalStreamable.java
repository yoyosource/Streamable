package de.yoyosource.streamable3.internal;

public interface InternalStreamable {

    InternalStreamable setNext(StreamableConsumer streamableConsumer);
    <T> T evaluate();

    int getMaxParallelTasks();
    void setMaxParallelTasks(int maxParallelTasks);
}
