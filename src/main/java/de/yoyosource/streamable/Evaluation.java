package de.yoyosource.streamable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public sealed interface Evaluation permits Evaluation.SelfValue, Evaluation.Combined {

    /**
     * An unordered Stream has the Elements in any order.
     * The order is not deterministic and can vary from
     * call to call.
     */
    SelfValue UNORDERED = new SelfValue("UNORDERED", 0b00000001);

    /**
     * An Ordered Stream has the Elements in the order
     * it was originally supplied in. Elements can still
     * be evaluated in an unpredictable manner for parallel
     * execution.
     */
    SelfValue ORDERED = new SelfValue("ORDERED", 0b00000010);

    /**
     * The current step will not be evaluated in parallel
     * regardless what was supplied to a previous
     * {@link Streamable#parallel(int)} call. Only one single
     * {@link Thread} will be used to evaluate the step.
     */
    Value SEQUENTIAL = new SelfValue("SEQUENTIAL", 0b00000100);

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
    Value CONCURRENT = new SelfValue("CONCURRENT", 0b00001000);

    /**
     * Using this Evaluation flag it will be assumed that the
     * {@link StreamableGatherer#integrate(Object, long, Object, Consumer)}
     * and {@link StreamableCollector#accumulate(Object, long, Object)}
     * method both never return {@code false} thus ignoring the
     * result.
     */
    Value GREEDY = new SelfValue("GREEDY", 0b00010000);

    /**
     * Using this Evaluation flag it will be assumed that the
     * {@link StreamableGatherer#container()} can be safely ignored.
     */
    Value NO_CONTAINER = new SelfValue("NO_CONTAINER", 0b00100000);

    static Evaluation get(Value first, Value... others) {
        if (others.length == 0) {
            if (first == UNORDERED) {
                return UNORDERED;
            } else if (first == ORDERED) {
                return ORDERED;
            }
        }

        int combinedIdentifier = UNORDERED.identifier;
        combinedIdentifier |= first.identifier;
        if (first == ORDERED) {
            combinedIdentifier &= ~UNORDERED.identifier;
        }

        for (Value other : others) {
            combinedIdentifier |= other.identifier;
            if (other == ORDERED) {
                combinedIdentifier &= ~UNORDERED.identifier;
            } else if (other == UNORDERED) {
                combinedIdentifier &= ~ORDERED.identifier;
            }
        }

        if (Combined.COMBINED[combinedIdentifier] == null) {
            Combined.COMBINED[combinedIdentifier] = new Combined(combinedIdentifier);
        }
        return Combined.COMBINED[combinedIdentifier];
    }

    static Value valueOf(String name) {
        for (Value value : Value.VALUES) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No enum constant " + Evaluation.class.getCanonicalName() + "." + name);
    }

    static Value[] values() {
        return Value.VALUES.toArray(new Value[0]);
    }

    default Evaluation with(Value first, Value... others) {
        int identifier = ((Base) this).identifier;

        identifier |= first.identifier;
        if (first == ORDERED) {
            identifier &= ~UNORDERED.identifier;
        } else if (first == UNORDERED) {
            identifier &= ~ORDERED.identifier;
        }

        for (Value other : others) {
            identifier |= other.identifier;
            if (other == ORDERED) {
                identifier &= ~UNORDERED.identifier;
            } else if (other == UNORDERED) {
                identifier &= ~ORDERED.identifier;
            }
        }

        if (Combined.COMBINED[identifier] == null) {
            Combined.COMBINED[identifier] = new Combined(identifier);
        }
        return Combined.COMBINED[identifier];
    }

    default Evaluation without(Value first, Value... others) {
        int identifier = ((Base) this).identifier & ~((Base) Combined.NONE.with(first, others)).identifier;
        if ((identifier & UNORDERED.identifier) == 0 && (identifier & ORDERED.identifier) == 0) {
            identifier |= UNORDERED.identifier;
        }

        if (Combined.COMBINED[identifier] == null) {
            Combined.COMBINED[identifier] = new Combined(identifier);
        }
        return Combined.COMBINED[identifier];
    }

    default boolean contains(Value first, Value... others) {
        int check = ((Base)Combined.NONE.with(first, others)).identifier;
        int identifier = ((Base)this).identifier;
        return (identifier & check) == check;
    }

    sealed class Base {

        protected final int identifier;

        private Base(int identifier) {
            this.identifier = identifier;
        }
    }

    sealed class Value extends Base {

        private static final List<Value> VALUES = new ArrayList<>();

        private static int ordinalCounter = 0;

        private final String name;
        private final int ordinal;

        private Value(String name, int identifier) {
            super(identifier);
            this.name = name;
            this.ordinal = ordinalCounter++;
            VALUES.add(this);
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
    }

    final class SelfValue extends Value implements Evaluation {

        private SelfValue(String name, int identifier) {
            super(name, identifier);
        }
    }

    final class Combined extends Base implements Evaluation {

        private static final Combined NONE = new Combined(0b00000000);
        private static final Combined[] COMBINED = new Combined[1 << Value.VALUES.size()];

        private Combined(int identifier) {
            super(identifier);
        }

        @Override
        public String toString() {
            StringBuilder st = new StringBuilder();
            for (Evaluation.Value value : Value.VALUES) {
                if ((identifier & value.identifier) != 0) {
                    if (!st.isEmpty()) {
                        st.append(", ");
                    }
                    st.append(value);
                }
            }
            return "Evaluation[" + identifier + ": " + st + ']';
        }
    }
}
