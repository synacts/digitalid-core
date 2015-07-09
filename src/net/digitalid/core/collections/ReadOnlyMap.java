package net.digitalid.core.collections;

import java.util.Map;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Freezable;
import net.digitalid.core.interfaces.Immutable;
import net.digitalid.core.interfaces.ReadOnly;

/**
 * This interface provides read-only access to {@link Map maps} and should <em>never</em> be cast away (unless external code requires it).
 * Please note that {@link Map#entrySet()} cannot be supported because it is not possible to return a covariant generic type.
 * <p>
 * <em>Important:</em> Only use immutable types for the keys and freezable or immutable types for the values!
 * (The types are not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableMap
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyMap<K,V> extends ReadOnly {
    
    /**
     * @see Map#size()
     */
    @Pure
    public int size();
    
    /**
     * @see Map#isEmpty()
     */
    @Pure
    public boolean isEmpty();
    
    /**
     * Returns whether this map is not empty.
     * 
     * @return whether this map is not empty.
     */
    @Pure
    public boolean isNotEmpty();
    
    /**
     * @see Map#containsKey(java.lang.Object)
     */
    @Pure
    public boolean containsKey(Object key);
    
    /**
     * @see Map#containsValue(java.lang.Object)
     */
    @Pure
    public boolean containsValue(Object value);
    
    /**
     * @see Map#get(java.lang.Object)
     */
    @Pure
    public V get(Object key);
    
    /**
     * @see Map#keySet()
     */
    @Pure
    public ReadOnlySet<K> keySet();
    
    /**
     * @see Map#values()
     */
    @Pure
    public ReadOnlyCollection<V> values();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableMap<K,V> clone();
    
}
