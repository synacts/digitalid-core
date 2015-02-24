package net.digitalid.core.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;

/**
 * Implements a concurrent hash set based on {@link ConcurrentHashMap}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements ConcurrentSet<E> {
    
    /**
     * Stores the elements of this concurrent hash set.
     */
    private final @Nonnull ConcurrentHashMap<E, Boolean> map;
    
    /**
     * Stores the set representation of the map.
     */
    private final @Nonnull Set<E> set;
    
    /**
     * @see ConcurrentHashMap#ConcurrentHashMap(int, float, int)
     */
    public ConcurrentHashSet(int initialCapacity, float loadFactor, int concurrencyLevel) {
        this.map = new ConcurrentHashMap<E, Boolean>(initialCapacity, loadFactor, concurrencyLevel);
        this.set = map.keySet();
    }
    
    /**
     * @see ConcurrentHashMap#ConcurrentHashMap(int, float)
     */
    public ConcurrentHashSet(int initialCapacity, float loadFactor) {
        this.map = new ConcurrentHashMap<E, Boolean>(initialCapacity, loadFactor);
        this.set = map.keySet();
    }
    
    /**
     * @see ConcurrentHashMap#ConcurrentHashMap(int)
     */
    public ConcurrentHashSet(int initialCapacity) {
        this.map = new ConcurrentHashMap<E, Boolean>(initialCapacity);
        this.set = map.keySet();
    }
    
    /**
     * @see ConcurrentHashMap#ConcurrentHashMap()
     */
    public ConcurrentHashSet() {
        this.map = new ConcurrentHashMap<E, Boolean>();
        this.set = map.keySet();
    }
    
    /**
     * @see ConcurrentHashMap#ConcurrentHashMap(java.util.Map)
     */
    public ConcurrentHashSet(@Nonnull Set<? extends E> set) {
        this.map = new ConcurrentHashMap<E, Boolean>(set.size());
        this.set = map.keySet();
        addAll(set);
    }
    
    
    @Pure
    @Override
    public int size() {
        return map.size();
    }
    
    @Pure
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    @Pure
    @Override
    public boolean isNotEmpty() {
        return !map.isEmpty();
    }
    
    @Pure
    @Override
    public boolean contains(@Nonnull Object object) {
        return map.containsKey(object);
    }
    
    @Pure
    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }
    
    @Pure
    @Override
    public Object[] toArray() {
        return set.toArray();
    }
    
    @Pure
    @Override
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] toArray(@Nonnull T[] array) {
        return set.toArray(array);
    }
    
    
    @Override
    public boolean add(@Nonnull E element) {
        return map.put(element, true) == null;
    }
    
    @Override
    public boolean remove(@Nonnull Object object) {
        return map.remove(object) != null;
    }
    
    
    @Pure
    @Override
    public boolean containsAll(@Nonnull Collection<?> collection) {
        return set.containsAll(collection);
    }
    
    @Override
    public boolean addAll(@Nonnull Collection<? extends E> collection) {
        boolean changed = false;
        for (@Nonnull E element : collection) if (add(element)) changed = true;
        return changed;
    }
    
    @Override
    public boolean retainAll(@Nonnull Collection<?> collection) {
        boolean changed = false;
        for (@Nonnull E element : this) {
            if (!collection.contains(element)) {
                remove(element);
                changed = true;
            }
        }
        return changed;
    }
    
    @Override
    public void clear() {
        map.clear();
    }
    
}
