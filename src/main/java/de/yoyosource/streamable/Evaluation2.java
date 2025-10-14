package de.yoyosource.streamable;

import java.io.Serializable;
import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Evaluation2 implements Serializable {

    final int identifier;

    private Evaluation2(int identifier) {
        this.identifier = identifier;
    }

    private static final List<Pure> PURE = new ArrayList<>();

    public static class Pure extends Evaluation2 implements Constable, Comparable<Pure> {

        private static int ORDINAL_COUNTER = 0;
        private final String name;
        private final int ordinal;

        private Pure(String name, int identifier) {
            super(identifier);
            this.name = name;
            this.ordinal = ORDINAL_COUNTER++;
            PURE.add(this);
        }

        public String name() {
            return name;
        }

        public int ordinal() {
            return ordinal;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public Optional<? extends ConstantDesc> describeConstable() {
            DirectMethodHandleDesc bootstrapMethod = MethodHandleDesc.ofField(DirectMethodHandleDesc.Kind.STATIC_GETTER, ClassDesc.of("de.yoyosource.streamable.Evaluation2"), name, ClassDesc.of("de.yoyosource.streamable.Evaluation2$Pure"));
            return Optional.of(DynamicConstantDesc.ofNamed(bootstrapMethod, name, ClassDesc.of("de.yoyosource.streamable.Evaluation2.Pure")));
        }

        @Override
        public int compareTo(Pure other) {
            return this.ordinal - other.ordinal;
        }
    }

    public static Pure[] values() {
        return PURE.toArray(new Pure[0]);
    }

    public static Pure valueOf(String name) {
        for (Pure pure : PURE) {
            if (pure.name.equals(name)) {
                return pure;
            }
        }
        throw new IllegalArgumentException("No enum constant Evaluation." + name);
    }

    private static final Evaluation2 NONE = new Evaluation2(0b00000000) {

        @Override
        public String toString() {
            return "<NONE>";
        }
    };

    /**
     * An unordered Stream has the Elements in any order.
     * The order is not deterministic and can vary from
     * call to call.
     */
    public static final Pure UNORDERED = new Pure("UNORDERED", 0b00000001) {
    };

    /**
     * An Ordered Stream has the Elements in the order
     * it was originally supplied in. Elements can still
     * be evaluated in an unpredictable manner for parallel
     * execution.
     */
    public static final Pure ORDERED = new Pure("ORDERED", 0b00000010) {
    };

    /**
     * The current step will not be evaluated in parallel
     * regardless what was supplied to a previous
     * {@link Streamable#parallel(int)} call. Only one single
     * {@link Thread} will be used to evaluate the step.
     */
    public static final Pure SEQUENTIAL = new Pure("SEQUENTIAL", 0b00000100) {
    };

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
     */
    public static final Pure CONCURRENT = new Pure("CONCURRENT", 0b00001000) {
    };

    /**
     * Using this Evaluation flag it will be assumed that the
     * {@link StreamableGatherer#integrate(Object, long, Object, Consumer)}
     * and {@link StreamableCollector#accumulate(Object, long, Object)}
     * method both never return {@code false} thus ignoring the
     * result.
     */
    public static final Pure GREEDY = new Pure("GREEDY", 0b00010000) {
    };

    /**
     * Using this Evaluation flag it will be assumed that the
     * {@link StreamableGatherer#container()} can be safely ignored.
     */
    public static final Pure NO_CONTAINER = new Pure("NO_CONTAINER", 0b00100000) {
    };

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        for (Pure pure : PURE) {
            if ((identifier & pure.identifier) != 0) {
                if (!st.isEmpty()) {
                    st.append(", ");
                }
                st.append(pure);
            }
        }
        return "Evaluation[" + identifier + ": " + st + ']';
    }

    private static final Evaluation2[] EVALUATIONS = new Evaluation2[1 << PURE.size()];

    static {
        for (Evaluation2 pure : PURE) {
            EVALUATIONS[pure.identifier] = pure;
        }
    }

    public static Evaluation2 get(Pure pure, Pure... evaluations) {
        return Evaluation2.UNORDERED.with(pure, evaluations);
    }

    public Evaluation2 with(Pure pure, Pure... evaluations) {
        int id = identifier;
        if (pure == ORDERED) {
            id = id & ~UNORDERED.identifier;
        } else if (pure == UNORDERED) {
            id = id & ~ORDERED.identifier;
        }
        id |= pure.identifier;

        for (Pure pure2 : evaluations) {
            if (pure2 == ORDERED) {
                id = id & ~UNORDERED.identifier;
            } else if (pure2 == UNORDERED) {
                id = id & ~ORDERED.identifier;
            }
            id |= pure2.identifier;
        }
        if (EVALUATIONS[id] == null) {
            EVALUATIONS[id] = new Evaluation2(id);
        }
        return EVALUATIONS[id];
    }

    public Evaluation2 without(Pure pure, Pure... evaluations) {
        int id = identifier & ~NONE.with(pure, evaluations).identifier;
        if ((id & UNORDERED.identifier) == 0 && (id & ORDERED.identifier) == 0) {
            id |= UNORDERED.identifier;
        }
        if (EVALUATIONS[id] == null) {
            EVALUATIONS[id] = new Evaluation2(id);
        }
        return EVALUATIONS[id];
    }

    public boolean contains(Pure pure, Pure... evaluations) {
        return contains(Evaluation2.NONE.with(pure, evaluations));
    }

    public boolean contains(Evaluation2 evaluation2) {
        return (identifier & evaluation2.identifier) == evaluation2.identifier;
    }
}
