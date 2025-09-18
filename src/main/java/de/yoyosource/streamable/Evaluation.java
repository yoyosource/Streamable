package de.yoyosource.streamable;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public enum Evaluation {
    /**
     * The current step will not be evaluated in parallel
     * regardless what was supplied to a previous
     * {@link Streamable#parallel(int)} call. Only one single
     * {@link Thread} will be used to evaluate the step.
     */
    SEQUENTIAL,

    /**
     * Using this Evaluation flag it will be assumed that the
     * {@link StreamableGatherer#combine(Object, Object)} method
     * and the {@link StreamableCollector#combine(Object, Object)}
     * method can be called simultaneously from multiple {@link Thread}'s
     * to combine values. Both methods must return the first
     * parameter as there return value. This also assumes that the
     * {@link StreamableGatherer#integrate(Object, long, Object, Consumer)}
     * method and the {@link StreamableCollector#accumulate(Object, long, Object)}
     * method can receive a container used in another {@link Thread}
     * simultaneously.
     *
     * @implNote Currently, this only takes effect in conjunction with {@link #GREEDY}.
     */
    CONCURRENT,

    /**
     * Using this Evaluation flag it will be assumed that the
     * {@link StreamableGatherer#integrate(Object, long, Object, Consumer)}
     * and {@link StreamableCollector#accumulate(Object, long, Object)}
     * method both never return {@code false} thus ignoring the
     * result.
     */
    GREEDY,

    /**
     * Using this Evaluation flag it will be assumed that the
     * {@link StreamableGatherer#container()} can be safely ignored.
     */
    NO_CONTAINER,
    ;

    public static final Set<Evaluation> none = Collections.emptySet();

    public static final Set<Evaluation> sequential = Set.of(SEQUENTIAL);
    public static final Set<Evaluation> greedy = Set.of(GREEDY);
    public static final Set<Evaluation> concurrent = Set.of(CONCURRENT);
    public static final Set<Evaluation> noContainer = Set.of(NO_CONTAINER);

    public static final Set<Evaluation> sequential_greedy = Set.of(SEQUENTIAL, GREEDY);
    public static final Set<Evaluation> sequential_concurrent = Set.of(SEQUENTIAL, CONCURRENT);
    public static final Set<Evaluation> greedy_concurrent = Set.of(GREEDY, CONCURRENT);
    public static final Set<Evaluation> sequential_greedy_concurrent = Set.of(SEQUENTIAL, GREEDY, CONCURRENT);
    public static final Set<Evaluation> sequential_noContainer = Set.of(SEQUENTIAL, NO_CONTAINER);
}
