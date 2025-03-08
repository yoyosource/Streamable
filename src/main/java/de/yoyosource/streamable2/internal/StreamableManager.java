package de.yoyosource.streamable2.internal;

import de.yoyosource.streamable2.Streamable;
import de.yoyosource.streamable2.StreamableCollector;
import de.yoyosource.streamable2.StreamableGatherer;
import de.yoyosource.streamable2.internal.evaluator.DecoratedEvaluator;
import de.yoyosource.streamable2.internal.evaluator.SourceEvaluator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Spliterator;
import java.util.function.Consumer;

public class StreamableManager {

    private static final StreamableGatherer NOOP = new StreamableGatherer.Simple() {
        @Override
        public boolean integrate(Object element, long index, Consumer next) {
            next.accept(element);
            return false;
        }

        @Override
        public void finish(Consumer next) {

        }
    };

    private StreamableManager() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> Streamable<T> from(Spliterator<T> spliterator) {
        return from(new SourceEvaluator(spliterator).sequential(), Streamable.class);
    }

    private static <T, S extends Streamable<T>> S from(DecoratedEvaluator evaluator, Class<S> clazz) {
        long layer = evaluator.getLayer();
        return clazz.cast(Proxy.newProxyInstance(StreamableManager.class.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            if (is(method, "toString")) {
                return clazz.getTypeName() + "@" + System.identityHashCode(proxy);
            }
            if (layer != evaluator.getLayer()) {
                throw new IllegalStateException("This Streamable is already mutated. You cannot add another operation on this instance.");
            }
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }

            if (is(method, "spliterator")) {
                Spliterator spliterator = evaluator.spliterator();
                evaluator.add(NOOP, false);
                return spliterator;
            }
            if (is(method, "iterator")) {
                Spliterator spliterator = evaluator.spliterator();
                evaluator.add(NOOP, false);
                // return new SpliteratorIterator(spliterator);
                throw new UnsupportedOperationException("Not yet implemented!");
            }

            if (is(method, "as", Class.class)) {
                Class<S> type = (Class<S>) args[0];
                if (type.isInstance(proxy)) return proxy;
                return from(evaluator, type);
            }

            if (is(method, "gather", StreamableGatherer.class)) {
                evaluator.add((StreamableGatherer) args[0], false);
                return from(evaluator, clazz);
            }
            if (is(method, "flatGather", StreamableGatherer.class)) {
                evaluator.add((StreamableGatherer) args[0], true);
                return from(evaluator, clazz);
            }
            if (is(method, "collect", StreamableCollector.class)) {
                return evaluator.collect((StreamableCollector) args[0]);
            }

            if (is(method, "sequential")) {
                DecoratedEvaluator next = evaluator.sequential();
                if (next == evaluator) return proxy;
                return from(next, clazz);
            }
            if (is(method, "parallel")) {
                DecoratedEvaluator next = evaluator.parallel();
                if (next == evaluator) return proxy;
                return from(next, clazz);
            }

            throw new NoSuchMethodException("Method " + method.getName() + " not implemented!");
        }));
    }

    private static boolean is(Method method, String name, Class<?>... args) {
        if (!method.getName().equals(name)) return false;
        Class<?>[] classes = method.getParameterTypes();
        if (classes.length != args.length) return false;
        for (int i = 0; i < classes.length; i++) {
            if (!classes[i].isAssignableFrom(args[i])) return false;
        }
        return true;
    }
}
