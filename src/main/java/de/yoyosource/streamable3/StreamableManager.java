package de.yoyosource.streamable3;

import de.yoyosource.streamable3.internal.StreamableSupplier;
import de.yoyosource.streamable3.internal.root.Root;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Iterator;

public class StreamableManager {

    private StreamableManager() {
        throw new IllegalStateException("Utility class");
    }

    private static class StreamData {
        private StreamableSupplier supplier;
        private final Root root;
        private int maxParallelTasks = 1;

        public StreamData(Root root) {
            this.root = root;
            this.supplier = root;
        }
    }

    protected static <T, S extends Streamable<S, T>> Streamable<S, T> from(Iterator<T> source) {
        return from(new StreamData(new Root(source)), Streamable.class);
    }

    private static <T, S extends Streamable<S, T>> S from(StreamData streamData, Class<S> clazz) {
        Object object = Proxy.newProxyInstance(StreamableManager.class.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            // Methods of Object
            if (is(method, "toString")) {
                return clazz.getTypeName() + "@" + System.identityHashCode(proxy);
            }
            if (is(method, "hashCode")) {
                return System.identityHashCode(proxy);
            }
            if (is(method, "equals", Object.class)) {
                return proxy == args[0];
            }

            // Methods of Iterable
            if (is(method, "iterator")) {

            }

            // Methods of Streamable
            if (is(method, "as", Class.class)) {
                Class<S> type = (Class<S>) args[0];
                if (type.isInstance(proxy)) return proxy;
                return from(streamData, type);
            }

            if (is(method, "sequential")) {
                streamData.maxParallelTasks = 1;
                return proxy;
            }
            if (is(method, "parallel", int.class)) {
                int maxParallelTasks = (int) args[0];
                if (maxParallelTasks < 2) {
                    throw new IllegalArgumentException("maxParallelism must be greater than or equal to 2");
                }
                streamData.maxParallelTasks = maxParallelTasks;
                return proxy;
            }

            if (is(method, "gather", StreamableGatherer.class)) {
                streamData.supplier = streamData.supplier.setNext(streamData.maxParallelTasks, (StreamableGatherer) args[0]);
                return proxy;
            }
            if (is(method, "flatGather", StreamableGatherer.class)) {
                streamData.supplier = streamData.supplier.setNext(streamData.maxParallelTasks, (StreamableGatherer) args[0])
                        .setNext(0, (StreamableGatherer) null);
                return proxy;
            }
            if (is(method, "collect", StreamableCollector.class)) {
                return streamData.supplier.setNext(streamData.maxParallelTasks, (StreamableCollector) args[0]);
            }

            // Methods of sub classes
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }
            System.out.println(method + " " + Arrays.toString(args));
            throw new NoSuchMethodException("Method " + method.getName() + " not implemented!");
        });
        return clazz.cast(object);
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
