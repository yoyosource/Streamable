package de.yoyosource.streamable;

import de.yoyosource.streamable.internal.CloseException;
import de.yoyosource.streamable.internal.Evaluator;
import de.yoyosource.streamable.internal.GroupException;
import de.yoyosource.streamable.internal.InternalStreamable;
import de.yoyosource.streamable.internal.StreamableConsumer;
import de.yoyosource.streamable.internal.StreamableSupplier;
import de.yoyosource.streamable.internal.finish.Finish;
import de.yoyosource.streamable.internal.root.Root;
import de.yoyosource.streamable.internal.step.FlattenStep;
import de.yoyosource.streamable.internal.step.ParallelStep;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
        private List<Runnable> closeHandlers = new ArrayList<>();

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
                final List<Object> data = new LinkedList<>();
                streamData.supplier.setNext(1, new StreamableCollector.Simple<>() {
                    @Override
                    public Ordering ordering() {
                        return Ordering.ORDERED;
                    }

                    @Override
                    public boolean accumulate(long index, Object element) {
                        synchronized (data) {
                            data.add(element);
                        }
                        return false;
                    }

                    @Override
                    public Object finish() {
                        return null;
                    }
                });

                Finish finish = checkIfAllowedToEvaluate(method, streamData);
                List<Evaluator> evaluators = listEvaluators(streamData);
                setOrderingOfParallelSteps(streamData);

                int maxBacklogPerEvaluator = 100_000_000 / evaluators.size();

                return new Iterator<>() {
                    private boolean closed = false;

                    private void generateNext() {
                        for (int i = evaluators.size() - 1; i >= 0; i--) {
                            Evaluator evaluator = evaluators.get(i);
                            evaluator.evaluateNext();
                            if (evaluator.backlogSize() > maxBacklogPerEvaluator) break;
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        if (closed) {
                            closed = true;
                            return false;
                        }
                        while (true) {
                            synchronized (data) {
                                if (!data.isEmpty()) break;
                                if (finish.getResult() != null) {
                                    close(streamData);
                                    break;
                                }
                            }
                            generateNext();
                            if (streamData.root.getError() != null) {
                                if (closed || streamData.root.getError() instanceof CloseException) {
                                    closed = true;
                                    return false;
                                } else {
                                    unsafe.throwException(streamData.root.getError());
                                }
                            }
                        }
                        synchronized (data) {
                            return !data.isEmpty();
                        }
                    }

                    @Override
                    public Object next() {
                        if (closed) {
                            throw new IllegalStateException("No next Element present!");
                        }
                        synchronized (data) {
                            return data.removeFirst();
                        }
                    }
                };
            }

            // Methods of Streamable
            if (is(method, "as", Class.class)) {
                Class<S> type = (Class<S>) args[0];
                if (type.isInstance(proxy)) return proxy;
                return from(streamData, type);
            }

            if (is(method, "gather", StreamableGatherer.class)) {
                streamData.supplier = streamData.supplier.setNext(streamData.maxParallelTasks, (StreamableGatherer) args[0]);
                return proxy;
            }
            if (is(method, "flatGather", StreamableGatherer.class)) {
                streamData.supplier = streamData.supplier.setNext(streamData.maxParallelTasks, (StreamableGatherer) args[0])
                        .setNext(new FlattenStep());
                return proxy;
            }
            if (is(method, "collect", StreamableCollector.class)) {
                streamData.supplier.setNext(streamData.maxParallelTasks, (StreamableCollector) args[0]);
                return ((InternalStreamable) proxy).evaluate();
            }
            if (is(method, "onClose", Runnable.class)) {
                streamData.closeHandlers.add((Runnable)  args[0]);
                return proxy;
            }
            if (is(method, "close")) {
                close(streamData);
                return null;
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
                Finish finish = checkIfAllowedToEvaluate(method, streamData);
                List<Evaluator> evaluators = listEvaluators(streamData);
                setOrderingOfParallelSteps(streamData);

                int maxBacklogPerEvaluator = 100_000_000 / evaluators.size();

                while (finish.getResult() == null) {
                    if (streamData.root.getError() != null) {
                        if (streamData.root.getError() instanceof CloseException) {
                            return null;
                        } else {
                            unsafe.throwException(streamData.root.getError());
                        }
                    }
                    for (int i = evaluators.size() - 1; i >= 0; i--) {
                        Evaluator evaluator = evaluators.get(i);
                        evaluator.evaluateNext();
                        if (evaluator.backlogSize() > maxBacklogPerEvaluator) break;
                    }
                }
                close(streamData);
                return finish.getResult().get();
            }
            if (is(method, "getMaxParallelTasks")) {
                return streamData.maxParallelTasks;
            }
            if (is(method, "setMaxParallelTasks", int.class)) {
                streamData.maxParallelTasks = (int) args[0];
                return null;
            }
            if (is(method, "getCloseHandlers")) {
                return streamData.closeHandlers;
            }
            if (is(method, "addCloseHandler", List.class)) {
                streamData.closeHandlers.addAll((List<Runnable>) args[0]);
                return null;
            }

            // Methods of sub classes
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }
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

    private static Finish checkIfAllowedToEvaluate(Method method, StreamData streamData) {
        StreamableConsumer streamableConsumer = streamData.supplier.getNext();
        while (!(streamableConsumer instanceof Finish finish)) {
            if (streamableConsumer instanceof StreamableSupplier supplier) {
                streamableConsumer = supplier.getNext();
            } else {
                throw new UnsupportedOperationException(method.getName() + "() cannot be called when no Finish Object is present!");
            }
        }
        return finish;
    }

    private static List<Evaluator> listEvaluators(StreamData streamData) {
        List<Evaluator> evaluators = new ArrayList<>();
        StreamableSupplier streamableSupplier = streamData.root;
        while (streamableSupplier != null) {
            if (streamableSupplier instanceof Evaluator evaluator) {
                evaluators.add(evaluator);
            }
            if (streamableSupplier.getNext() instanceof StreamableSupplier supplier) {
                streamableSupplier = supplier;
            } else {
                streamableSupplier = null;
            }
        }
        return evaluators;
    }

    private static void setOrderingOfParallelSteps(StreamData streamData) {
        List<StreamableConsumer> consumers = new ArrayList<>();

        StreamableSupplier streamableSupplier = streamData.root;
        while (streamableSupplier != null) {
            if (streamableSupplier instanceof StreamableConsumer consumer) {
                consumers.addFirst(consumer);
            }
            if (streamableSupplier.getNext() instanceof StreamableSupplier supplier) {
                streamableSupplier = supplier;
            } else {
                if (streamableSupplier.getNext() instanceof StreamableConsumer consumer) {
                    consumers.addFirst(consumer);
                }
                streamableSupplier = null;
            }
        }

        // System.out.println(consumers);

        Ordering ordering = Ordering.UNORDERED;
        for (StreamableConsumer consumer : consumers) {
            // System.out.println(consumer + " " + ordering + " with " + consumer.ordering() + " -> " + ordering.or(consumer.ordering()));
            if (consumer instanceof ParallelStep parallelStep) {
                parallelStep.setSequenceType(ordering.ordered);
                // System.out.println("Set " + consumer + " ordering to " + ordering.ordered);
            }
            ordering = ordering.or(consumer.ordering());
        }
    }

    private static void close(StreamData streamData) {
        List<Throwable> throwables = new ArrayList<>();
        streamData.closeHandlers.forEach(runnable -> {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                throwables.add(throwable);
            }
        });
        if (throwables.isEmpty()) {
            streamData.root.setError(CloseException.INSTANCE);
        } else {
            streamData.root.setError(new GroupException(throwables));
        }
    }
}
