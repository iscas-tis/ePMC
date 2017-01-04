package epmc.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class OrderedSet<K> implements Set<K> {
    private final class OrderedSetIterator implements Iterator<K> {
        K last;
        Iterator<K> inner = order.iterator();
        
        @Override
        public boolean hasNext() {
            return inner.hasNext();
        }

        @Override
        public K next() {
            last = inner.next();
            return last;
        }

        @Override
        public void remove() {
            set.remove(last);
            inner.remove();
        }
        
    }
    
    private final List<K> order = new LinkedList<>();
    private final Set<K> set;
    
    public OrderedSet(Set<K> set) {
        this.set = set;
        for (K key : set) {
            order.add(key);
        }
    }
    
    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<K> iterator() {
        return new OrderedSetIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[order.size()];
        int index = 0;
        for (K key : order) {
            result[index] = key;
            index++;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        T[] result;
        if (order.size() <= a.length) {
            result = a;
        } else {
            result = (T[]) new Object[order.size()];
        }
        int index = 0;
        for (K key : order) {
            result[index] = (T) key;
            index++;
        }
        return result;
    }

    @Override
    public boolean add(K e) {
        if (set.contains(e)) {
            return false;
        }
        set.add(e);
        order.add(e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        order.remove(o);
        return set.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        boolean changed = false;
        for (K key : c) {
            changed |= add(key);
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        order.retainAll(c);
        return set.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object key : c) {
            changed |= remove(key);
        }
        return changed;
    }

    @Override
    public void clear() {
        set.clear();
        order.clear();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int index = 0;
        for (K entry : this) {
            builder.append(entry);
            if (index < size() - 1) {
                builder.append(",");
            }
            index++;
        }
        builder.append("}");
        return builder.toString();
    }
}
