package ch.virtualid.util;

import ch.virtualid.annotation.Capturable;
import ch.virtualid.annotation.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.Readonly;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link Map maps} and should <em>never</em> be cast away (unless external code requires it).
 * Please note that {@link Map#entrySet()} cannot be supported because it is not possible to return a covariant generic type.
 * <p>
 * <em>Important:</em> Only use immutable types for the keys and freezable or immutable types for the values!
 * (The types are not restricted to {@link Freezable} or {@link Immutable} so that library types can also be used.)
 * 
 * @see FreezableMap
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyMap<K,V> extends Readonly {
    
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
    public ReadonlySet<K> keySet();
    
    /**
     * @see Map#values()
     */
    @Pure
    public ReadonlyCollection<V> values();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableMap<K,V> clone();
    
}
