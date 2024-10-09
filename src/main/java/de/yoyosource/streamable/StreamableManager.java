package de.yoyosource.streamable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class StreamableManager {

    private static class Pair<A, B> {
        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "first=" + first +
                    ", second=" + second +
                    '}';
        }
    }

    private static class StreamData {
        private final Stream stream;
        private List<Pair<StreamableGatherer, Boolean>> gatherers = new ArrayList<>();

        private StreamData(Stream stream) {
            this.stream = stream;
        }

        private Object run(StreamableCollector collector) {
            List<Pair<Iterator, Integer>> iterators = new ArrayList<>();
            iterators.add(new Pair<>(stream.iterator(), 0));

            while (!iterators.isEmpty()) {
                Pair<Iterator, Integer> pair = null;
                for (int i = iterators.size() - 1; i >= 0; i--) {
                    Pair<Iterator, Integer> current = iterators.get(i);
                    if (current.first.hasNext()) {
                        pair = current;
                        break;
                    } else {
                        iterators.remove(i);
                    }
                }
                if (pair == null) break;
                apply(iterators, pair.first.next(), pair.second, !pair.first.hasNext(), collector);
            }
            return collector.finish();
        }

        private boolean apply(List<Pair<Iterator, Integer>> iterators, Object o, int index, boolean runFinished, StreamableCollector collector) {
            if (index >= gatherers.size()) {
                collector.apply(o);
                return false;
            }
            int removeUntil = iterators.size();
            AtomicBoolean ignoreRest = new AtomicBoolean(false);
            Pair<StreamableGatherer, Boolean> pair = gatherers.get(index);
            boolean finished = pair.first.apply(o, next -> {
                if (ignoreRest.get()) return;
                if (pair.second) {
                    iterators.add(new Pair<>(((Iterable) next).iterator(), index + 1));
                } else {
                    if (apply(iterators, next, index + 1, runFinished, collector)) {
                        ignoreRest.set(true);
                    }
                }
            });
            if (finished || runFinished) {
                if (finished) {
                    for (int i = removeUntil - 1; i >= 0; i--) {
                        iterators.remove(i);
                    }
                }

                for (int i = index; i < gatherers.size(); i++) {
                    AtomicBoolean isFinished = new AtomicBoolean(false);
                    ignoreRest.set(false);
                    final int finalI = i;
                    gatherers.get(finalI).first.finish(next -> {
                        isFinished.set(true);
                        if (ignoreRest.get()) return;
                        if (pair.second) {
                            iterators.add(new Pair<>(((Iterable) o).iterator(), finalI + 1));
                        } else {
                            if (apply(iterators, next, finalI + 1, true, collector)) {
                                ignoreRest.set(true);
                            }
                        }
                    });
                    if (isFinished.get()) break;
                }
            }
            return finished;
        }

        private Iterator<Object> iterator() {
            if (gatherers.isEmpty()) return stream.iterator();
            return new Iterator<>() {
                private List<Pair<Iterator, Integer>> iterators = new ArrayList<>();

                private boolean hasNext = false;
                private Object value;

                private StreamableCollector collector = new StreamableCollector() {
                    @Override
                    public void apply(Object input) {
                        hasNext = true;
                        value = input;
                    }

                    @Override
                    public Object finish() {
                        return null;
                    }
                };

                private void generateNext() {
                    hasNext = false;
                    while (true) {
                        Pair<Iterator, Integer> pair = null;
                        for (int i = iterators.size() - 1; i >= 0; i--) {
                            Pair<Iterator, Integer> current = iterators.get(i);
                            if (current.first.hasNext()) {
                                pair = current;
                                break;
                            } else {
                                iterators.remove(i);
                            }
                        }
                        if (pair == null) break;
                        if (apply(iterators, pair.first.next(), pair.second, !pair.first.hasNext(), collector)) {
                            iterators.clear();
                        }
                        if (hasNext) break;
                        if (iterators.isEmpty()) break;
                    }
                }

                {
                    iterators.add(new Pair<>(stream.iterator(), 0));
                    generateNext();
                }

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public Object next() {
                    Object temp = value;
                    generateNext();
                    return temp;
                }
            };
        }
    }

    protected static <T> Streamable<T> from(Stream<T> stream) {
        return from(new StreamData(stream), Streamable.class);
    }

    private static <T, S extends Streamable<T>> S from(StreamData streamData, Class<S> clazz) {
        Object object = Proxy.newProxyInstance(StreamableManager.class.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            if (method.getName().equals("toString") && method.getParameterCount() == 0) {
                return clazz.getTypeName() + "@" + System.identityHashCode(proxy);
            }
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }

            if (is(method, "iterator")) {
                return streamData.iterator();
            }
            if (is(method, "as", Class.class)) {
                Class<S> type = (Class<S>) args[0];
                if (type.isInstance(proxy)) return proxy;
                return from(streamData, type);
            }
            if (is(method, "gather", StreamableGatherer.class)) {
                streamData.gatherers.add(new Pair<>((StreamableGatherer) args[0], false));
                return proxy;
            }
            if (is(method, "flatGather", StreamableGatherer.class)) {
                streamData.gatherers.add(new Pair<>((StreamableGatherer) args[0], true));
                return proxy;
            }
            if (is(method, "collect", StreamableCollector.class)) {
                return streamData.run((StreamableCollector) args[0]);
            }
            throw new NoSuchMethodException("Method " + method.getName() + " not implemented!");
        });
        return (S) object;
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
