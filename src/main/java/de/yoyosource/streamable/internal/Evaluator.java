package de.yoyosource.streamable.internal;

public interface Evaluator {

    boolean evaluateNext();
    int backlogSize();
}
