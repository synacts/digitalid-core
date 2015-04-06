package net.digitalid.core.property.indexed;

import javax.annotation.Nonnull;
import net.digitalid.core.collections.ReadonlyCollection;
import net.digitalid.core.collections.ReadonlyMap;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract interface ReadOnlyIndexedProperty<K, V, R extends ReadonlyMap<K, V>> {
    
    public abstract @Nonnull V get(@Nonnull K key);
    
    public abstract @Nonnull ReadonlyCollection<V> getAll();
    
    public abstract @Nonnull R getMap();
    
}
