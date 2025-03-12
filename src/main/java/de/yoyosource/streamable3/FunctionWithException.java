package de.yoyosource.streamable3;

public interface FunctionWithException<T, R, E extends Throwable> {
    R apply(T t) throws E;
}
