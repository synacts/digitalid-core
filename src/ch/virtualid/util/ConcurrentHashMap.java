package ch.virtualid.util;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Extends Java's {@link java.util.concurrent.ConcurrentHashMap ConcurrentHashMap} implementation with more useful methods.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class ConcurrentHashMap<K, V> extends java.util.concurrent.ConcurrentHashMap<K, V> implements ConcurrentMap<K, V> {
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(int, float, int)
     */
    public ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(int, float)
     */
    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(int)
     */
    public ConcurrentHashMap(int initialCapacity) {
        super(initialCapacity);
    }
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap()
     */
    public ConcurrentHashMap() {
        super();
    }
    
    /**
     * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(java.util.Map)
     */
    public ConcurrentHashMap(@Nonnull Map<? extends K, ? extends V> m) {
        super(m);
    }
    
    
    @Override
    public @Nonnull V putIfAbsentElseReturnPresent(@Nonnull K key, @Nonnull V value) {
        @Nullable V previous = putIfAbsent(key, value);
        if (previous == null) return value;
        else return previous;
    }
    
}
