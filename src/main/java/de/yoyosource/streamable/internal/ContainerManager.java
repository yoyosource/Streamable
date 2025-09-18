package de.yoyosource.streamable.internal;

import de.yoyosource.streamable.Evaluation;
import de.yoyosource.streamable.StreamableGatherer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public abstract class ContainerManager {

    public static ContainerManager get(StreamableGatherer streamableGatherer, boolean greedy) {
        Set<Evaluation> evaluation = streamableGatherer.evaluation();
        if (evaluation.contains(Evaluation.NO_CONTAINER)) {
            return new Empty(streamableGatherer);
        }
        if ((evaluation.contains(Evaluation.GREEDY) && greedy)) {
            if (evaluation.contains(Evaluation.CONCURRENT)) {
                return new GreedyConcurrent(streamableGatherer);
            } else {
                return new Greedy(streamableGatherer);
            }
        } else if (evaluation.contains(Evaluation.CONCURRENT)) {
            return new Concurrent(streamableGatherer);
        } else {
            return new Base(streamableGatherer);
        }
    }

    protected final StreamableGatherer gatherer;

    protected ContainerManager(StreamableGatherer streamableGatherer) {
        this.gatherer = streamableGatherer;
    }

    public abstract Object get(long index);

    public abstract void set(long index, Object value);

    public abstract Object remove(long index);

    public abstract void combine(long index);

    public abstract int size();

    public boolean isEmpty() {
        return size() == 0;
    }

    public abstract Object getAny();

    public static class Base extends ContainerManager {
        protected final Map<Long, Object> containers = new HashMap<>();

        public Base(StreamableGatherer streamableGatherer) {
            super(streamableGatherer);
        }

        @Override
        public synchronized Object get(long index) {
            return containers.get(index);
        }

        @Override
        public synchronized void set(long index, Object value) {
            containers.put(index, value);
        }

        @Override
        public synchronized Object remove(long index) {
            return containers.remove(index);
        }

        @Override
        public synchronized void combine(long index) {
            List<Long> indices = new ArrayList<>(containers.size());
            for (long key : containers.keySet()) {
                if (key < index) indices.add(key);
            }
            indices.sort(Long::compareTo);

            if (indices.isEmpty()) return;

            for (int i = 0; i < indices.size() - 1; i++) {
                long i1 = indices.get(i);
                long i2 = indices.get(i + 1);
                Object c1 = containers.remove(i1);
                Object c2 = containers.remove(i2);
                Object cr = gatherer.combine(c1, c2);
                containers.put(Math.max(i1, i2), cr);
            }

            // Object c = containers.remove(indices.getLast());
            // containers.put(index - 1, c);
        }

        @Override
        public synchronized int size() {
            return containers.size();
        }

        @Override
        public synchronized Object getAny() {
            return containers.values().iterator().next();
        }
    }

    public static class Concurrent extends Base {
        public Concurrent(StreamableGatherer streamableGatherer) {
            super(streamableGatherer);
        }

        @Override
        public synchronized void combine(long index) {
            Queue<Object> queue = new LinkedList<>();
            Iterator<Long> keyIterator = containers.keySet().iterator();
            while (keyIterator.hasNext()) {
                Long key = keyIterator.next();
                if (key < index) {
                    queue.add(containers.get(key));
                    keyIterator.remove();
                }
            }

            if (queue.isEmpty()) return;

            while (queue.size() > 1) {
                Object first = queue.poll();
                Object second = queue.poll();
                Object cr = gatherer.combine(first, second);
                queue.add(cr);
            }

            containers.put(index - 1, queue.poll());
        }
    }

    public static class Greedy extends ContainerManager {
        private final Map<Thread, Object> containers = new HashMap<>();

        public Greedy(StreamableGatherer streamableGatherer) {
            super(streamableGatherer);
        }

        @Override
        public synchronized Object get(long index) {
            return containers.get(Thread.currentThread());
        }

        @Override
        public synchronized void set(long index, Object value) {
            containers.put(Thread.currentThread(), value);
        }

        @Override
        public synchronized Object remove(long index) {
            return containers.remove(Thread.currentThread());
        }

        @Override
        public synchronized void combine(long index) {
            Object container = null;
            for (Map.Entry<Thread, Object> entry : containers.entrySet()) {
                if (container == null) {
                    container = entry.getValue();
                } else {
                    container = gatherer.combine(container, entry.getValue());
                }
            }
            containers.clear();
            containers.put(Thread.currentThread(), container);
        }

        @Override
        public synchronized int size() {
            return containers.size();
        }

        @Override
        public synchronized Object getAny() {
            return containers.values().iterator().next();
        }
    }

    public static class GreedyConcurrent extends ContainerManager {
        private final Object container;

        public GreedyConcurrent(StreamableGatherer streamableGatherer) {
            super(streamableGatherer);
            this.container = streamableGatherer.container();
        }

        @Override
        public Object get(long index) {
            return container;
        }

        @Override
        public void set(long index, Object value) {
            // Ignore this should always be called with the same container the get method returns!
        }

        @Override
        public Object remove(long index) {
            return container;
        }

        @Override
        public void combine(long index) {
            // Do nothing there are no containers to combine!
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public synchronized Object getAny() {
            return container;
        }
    }

    public static class Empty extends ContainerManager {
        public Empty(StreamableGatherer streamableGatherer) {
            super(streamableGatherer);
        }

        @Override
        public Object get(long index) {
            return null;
        }

        @Override
        public void set(long index, Object value) {
        }

        @Override
        public Object remove(long index) {
            return null;
        }

        @Override
        public void combine(long index) {
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Object getAny() {
            return null;
        }
    }
}
