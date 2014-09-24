package ch.virtualid.util;

/**
 * Extends Java's {@link java.util.concurrent.ConcurrentMap ConcurrentMap} interface with more useful methods.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ConcurrentMap<K, V> extends java.util.concurrent.ConcurrentMap<K, V> {
    
    /**
     * Associates the given value with the given key, if the
     * given key is not already associated with a value.
     * 
     * @param key the key to be associated with the given value.
     * @param value the value to be associated with the given key.
     * 
     * @return the value that is now associated with the given key.
     */
    public V putIfAbsentReturnPresent(K key, V value);
    
}
