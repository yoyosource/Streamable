package de.yoyosource.streamable;

import lombok.AllArgsConstructor;

/**
 * This enum holds the ordering values for how the {@link Streamable} should be ordered.
 */
@AllArgsConstructor
public enum Ordering {
    /**
     * An unordered Stream has the Elements in any order.
     * The order is not deterministic and can vary from
     * call to call.
     */
    UNORDERED(false, false),

    /**
     * An Ordered Stream has the Elements in the order
     * it was originally supplied in. Elements can still
     * be evaluated in an unpredictable manner for parallel
     * execution.
     */
    ORDERED(true, false),

    /**
     * A Sequential Stream has the Elements in the order
     * it was originally supplied in. Elements will not
     * be evaluated in an unpredictable manner. No parallel
     * execution is possible and everything will be done
     * in the original order provided.
     */
    SEQUENTIAL(true, true),
    ;

    private static final Ordering[] VALUES = values();

    public final boolean ordered;
    public final boolean sequential;

    public Ordering or(Ordering ordering) {
        return VALUES[Math.max(ordinal(), ordering.ordinal())];
    }
}
