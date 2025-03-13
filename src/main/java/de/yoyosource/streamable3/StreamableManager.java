package de.yoyosource.streamable3;

import de.yoyosource.streamable3.internal.InternalStreamable;
import de.yoyosource.streamable3.internal.StreamableConsumer;
import de.yoyosource.streamable3.internal.StreamableSupplier;
import de.yoyosource.streamable3.internal.finish.Finish;
import de.yoyosource.streamable3.internal.root.Root;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Iterator;

public class StreamableManager {

    private static final Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

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
        Object object = Proxy.newProxyInstance(StreamableManager.class.getClassLoader(), new Class[]{clazz, InternalStreamable.class}, (proxy, method, args) -> {
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
                throw new UnsupportedOperationException("Iterator not implemented yet!");
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
            if (is(method, "isParallel")) {
                return streamData.maxParallelTasks > 1;
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
                streamData.supplier.setNext(streamData.maxParallelTasks, (StreamableCollector) args[0]);
                return ((InternalStreamable) proxy).evaluate();
            }

            // Methods of InternalStreamable
            if (is(method, "setNext", StreamableConsumer.class)) {
                StreamableConsumer streamableConsumer = streamData.supplier.setNext((StreamableConsumer) args[0]);
                if (streamableConsumer instanceof StreamableSupplier supplier) {
                    streamData.supplier = supplier;
                }
                return proxy;
            }
            if (is(method, "evaluate")) {
                if (!(streamData.supplier.getNext() instanceof Finish finish)) {
                    throw new UnsupportedOperationException("evaluate() cannot be called when no Finish Object is present!");
                }

                streamData.root.evaluate();
                while (finish.getResult() == null) {
                    if (streamData.root.getError() != null) {
                        unsafe.throwException(streamData.root.getError());
                    }
                    Thread.yield();
                }
                return finish.getResult().get();
            }
            if (is(method, "getMaxParallelTasks")) {
                return streamData.maxParallelTasks;
            }
            if (is(method, "setMaxParallelTasks", int.class)) {
                streamData.maxParallelTasks = (int) args[0];
                return null;
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
