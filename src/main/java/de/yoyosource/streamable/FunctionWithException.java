package de.yoyosource.streamable;

/**
 * Represents a function that accepts one argument and produces a result.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of the exception of the function
 */
@FunctionalInterface
public interface FunctionWithException<T, R, E extends Throwable> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @throws E the exception
     * @return the function result
     */
    R apply(T t) throws E;
}
