/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map ordered by order of the insertion of elements.
 * In contrast to the {@link LinkedHashMap},
 * <ul>
 * <li>an arbitrary map can be chosen as the base map to contain the elements
 * while the order is maintained by a linked list,</li>
 * <li>the insertion order is either the reversed order in which elements were
 * inserted or the reversed order,</li>
 * <li>ordering elements by the last access is not supported.</li>
 * </ul>
 * <p>
 * This class is serializable. It is not thread-safe.
 * </p>
 * <p>
 * This class exists because such a functionality is provided neither by Trove
 * nor by Google Guava. In case this functionality is added there, the class
 * should be replaced by an according class from one of these libraries.
 * </p>
 * 
 * @author Ernst Moritz Hahn
 *
 * @param <K> type of key objects
 * @param <V> type of value objects
 */
public final class OrderedMap<K, V> implements Map<K, V>, Serializable {
    private final class KeySetIterator implements Iterator<K> {
        private K last;
        private final Iterator<K> inner;

        private KeySetIterator() {
            this.inner = keys.iterator();
        }

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
            map.remove(last);
            inner.remove();
        }        
    }

    private final class KeySet implements Set<K>, Serializable {
        private static final long serialVersionUID = 1L;

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
            return new KeySetIterator();
        }

        @Override
        public Object[] toArray() {
            return keys.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return keys.toArray(a);
        }

        @Override
        public boolean add(K e) {
            assert false;
            return false;
        }

        @Override
        public boolean remove(Object o) {
            if (map.containsKey(o)) {
                keys.remove(o);
                map.remove(o);
                return true;
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object entry : c) {
                if (!map.containsKey(entry)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            assert false;
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean changed = false;
            Iterator<K> it = keys.iterator();
            while (it.hasNext()) {
                K key = it.next();
                if (!c.contains(key)) {
                    changed |= remove(key);
                    it.remove();
                }
            }
            return changed;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean changed = false;
            for (Object entry : c) {
                changed |= remove(entry);
            }
            return changed;
        }

        @Override
        public void clear() {
            map.clear();
            keys.clear();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            int index = 0;
            for (Entry<K, V> entry : entrySet()) {
                builder.append(entry.getKey());
                if (index < keys.size() - 1) {
                    builder.append(",");
                }
                index++;
            }
            builder.append("]");            
            return builder.toString();
        }
    }

    private final class ValueIterator implements Iterator<V> {
        private K last;
        private final Iterator<K> inner;

        private ValueIterator() {
            this.inner = keys.iterator();
        }

        @Override
        public boolean hasNext() {
            return inner.hasNext();
        }

        @Override
        public V next() {
            last = inner.next();
            return map.get(last);
        }

        @Override
        public void remove() {
            map.remove(last);
            inner.remove();
        }

    }

    private final class Values implements Collection<V>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int size() {
            return map.values().size();
        }

        @Override
        public boolean isEmpty() {
            return map.values().isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return map.values().contains(o);
        }

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public Object[] toArray() {
            Object[] result = new Object[map.size()];
            int index = 0;
            for (K key : keys) {
                result[index] = map.get(key);
                index++;
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            T[] result;
            if (map.size() <= a.length) {
                result = a;
            } else {
                result = (T[]) new Object[map.size()];
            }
            int index = 0;
            for (K key : keys) {
                result[index] = (T) map.get(key);
                index++;
            }
            return result;
        }

        @Override
        public boolean add(V e) {
            assert false;
            return false;
        }

        @Override
        public boolean remove(Object o) {
            Iterator<K> it = keys.iterator();
            while (it.hasNext()) {
                K key = it.next();
                if (map.containsKey(key)) {
                    it.remove();
                    map.remove(key);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return map.values().containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends V> c) {
            assert false;
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean removed = false;
            for (Object entry : c) {
                removed |= remove(entry);
            }
            return removed;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean changed = false;
            Iterator<K> it = keys.iterator();
            while (it.hasNext()) {
                K key = it.next();
                V value = map.get(key);
                if (!c.contains(value)) {
                    changed |= remove(key);
                    it.remove();
                }
            }
            return changed;
        }

        @Override
        public void clear() {
            map.clear();
            keySet.clear();
        }

    }

    private final class OEntry implements Entry<K,V> {
        K key;
        boolean removed;

        public OEntry(K key) {
            this.key = key;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return map.get(key);
        }

        @Override
        public V setValue(V value) {
            return map.put(key, value);
        }

    }

    private final class EntrySetIterator implements Iterator<Entry<K, V>> {
        private K last;
        private final Iterator<K> inner;

        private EntrySetIterator() {
            inner = keys.iterator();
        }

        @Override
        public boolean hasNext() {
            return inner.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            last = inner.next();
            return new OEntry(last);
        }

        @Override
        public void remove() {
            map.remove(last);
            inner.remove();
        }

    }

    private final class EntrySet implements Set<Entry<K, V>>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof OrderedMap.OEntry)){
                return false;
            }
            OEntry entry = (OrderedMap.OEntry) o;
            return !entry.removed;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntrySetIterator();
        }

        @Override
        public Object[] toArray() {
            assert false;
            return null;
        }

        @Override
        public <T> T[] toArray(T[] a) {
            assert false;
            return null;
        }

        @Override
        public boolean add(java.util.Map.Entry<K, V> e) {
            assert false;
            return false;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(Object o) {
            if (!(o instanceof OrderedMap.OEntry)) {
                return false;
            }
            OEntry entry = (OEntry) o;
            if (entry.removed) {
                return false;
            } else {
                keys.remove(entry.key);
                entry.removed = true;
                return true;
            }
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object entry : c) {
                if (!contains(entry)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends java.util.Map.Entry<K, V>> c) {
            assert false;
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            assert false;
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean changed = false;
            for (Object entry : c) {
                changed |= remove(entry);
            }
            return changed;
        }

        @Override
        public void clear() {
            map.clear();
            keys.clear();
        }

    }

    /** serival version uid - not sure whether there is a better choice */
    private static final long serialVersionUID = 1L;
    /** underlying map of this ordered map */
    private final Map<K, V> map;
    /** list of keys to maintain the order of the map */
    private final List<K> keys = new LinkedList<>();
    private final KeySet keySet;
    private final Values values;
    private final EntrySet entrySet;
    /** whether reversed insertion order shall be used */
    private final boolean reversed;

    /**
     * Construct new ordered map.
     * 
     * @param map underlying map of this map
     * @param reversed whether insertion order shall be reversed
     */
    public OrderedMap(Map<K, V> map, boolean reversed) {
        assert map != null;
        this.map = map;
        this.reversed = reversed;
        for (Entry<K, V> entry : map.entrySet()) {
            if (reversed) {
                keys.add(0, entry.getKey());
            } else {
                keys.add(entry.getKey());
            }
        }
        this.keySet = new KeySet();
        this.values = new Values();
        this.entrySet = new EntrySet();
    }


    /**
     * Construct new ordered map.
     * The insertion order will not be reversed.
     * 
     * @param map underlying map of this map
     */
    public OrderedMap(Map<K, V> map) {
        this(map, false);
    }

    /**
     * Construct new ordered map.
     * The insertion order will not be reversed.
     * A newly constructed {@link HashMap} will be used as the underlying map.
     */
    public OrderedMap() {
        this(new HashMap<>(), false);
    }

    /**
     * Construct new ordered map.
     * A newly constructed {@link HashMap} will be used as the underlying map.
     *
     * @param reversed whether insertion order shall be reversed
     */
    public OrderedMap(boolean reversed) {
        this(new HashMap<>(), reversed);
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
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        V oldValue = map.get(key);
        if (!map.containsKey(key)) {
            if (reversed) {
                keys.add(0,  key);
            } else {
                keys.add(key);
            }
        }
        map.put(key, value);
        return oldValue;
    }

    @Override
    public V remove(Object key) {
        V result = map.remove(key);
        keys.remove(key);
        return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        keys.clear();
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return keySet;
    }

    @Override
    public Collection<V> values() {
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return entrySet;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int index = 0;
        for (Entry<K, V> entry : entrySet()) {
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
            if (index < keys.size() - 1) {
                builder.append(",");
            }
            index++;
        }
        builder.append("]");
        return builder.toString();
    }
}
