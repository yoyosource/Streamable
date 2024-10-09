package de.yoyosource.streamable;

public interface FunctionWithException<T, R, E extends Throwable> {
    R apply(T t) throws E;
}
