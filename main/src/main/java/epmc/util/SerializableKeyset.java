package epmc.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Obtain serializable key set of a given map.
 * This class exists because the object returned by {@link Map#keySet()} is not
 * serializable, which is a problem in the way we want to use it. Note that in
 * contrast to the result of {@link Map#keySet()}, objects of this class are
 * read-only views in the key set of the given map.
 * 
 * @author Ernst Moritz Hahn
 *
 * @param <K> type of keys of map to obtain key set of
 * @param <V> type of values of map to obtain key set of
 */
public final class SerializableKeyset<K,V> implements Set<K>,Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<K,V> map;

    public SerializableKeyset(Map<K,V> map) {
        assert map != null;
        this.map = map;
    }
    
    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<K> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[map.size()];
        int index = 0;
        for (K key : map.keySet()) {
            result[index] = key;
            index++;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        T[] result;
        if (a.length >= map.size()) {
            result = a;
        } else {
            result = (T[]) new Object[map.size()];
        }
        int index = 0;
        for (K key : map.keySet()) {
            result[index] = (T) key;
            index++;
        }
        return result;
    }

    @Override
    public boolean add(K e) {
        assert false;
        return false;
    }

    @Override
    public boolean remove(Object o) {
        assert false;
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        assert c != null;
        for (Object entry : c) {
            if (!map.containsKey(entry)) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        assert false;
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        assert false;
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        assert false;
        return false;
    }

    @Override
    public void clear() {
        assert false;
    }

    @Override
    public String toString() {
        return map.keySet().toString();
    }
}
