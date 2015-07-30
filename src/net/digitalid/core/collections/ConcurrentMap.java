package net.digitalid.core.collections;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;

/**
 * Extends Java's {@link java.util.concurrent.ConcurrentMap ConcurrentMap} interface with more useful methods.
 * 
 * @see ConcurrentHashMap
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ConcurrentMap<K, V> extends java.util.concurrent.ConcurrentMap<K, V>, Cloneable {
    
    /**
     * Associates the given value with the given key, if the
     * given key is not already associated with a value.
     * 
     * @param key the key to be associated with the given value.
     * @param value the value to be associated with the given key.
     * 
     * @return the value that is now associated with the given key.
     */
    public @Nonnull V putIfAbsentElseReturnPresent(@Nonnull K key, @Nonnull V value);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns a shallow copy of this map (the keys and values themselves are not cloned).
     *
     * @return a shallow copy of this map
     */
    @Pure
    public @Capturable @Nonnull ConcurrentMap<K, V> clone();
    
}
