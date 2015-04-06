package net.digitalid.core.property.indexed;

import javax.annotation.Nonnull;
import net.digitalid.core.collections.ReadonlyMap;
import net.digitalid.core.property.PropertyObserver;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public interface IndexedPropertyObserver<K, V, R extends ReadonlyMap<K, V>> extends PropertyObserver {
    
    public void added(@Nonnull ReadOnlyIndexedProperty<K, V, R> property, @Nonnull K newKey, @Nonnull V newValue);
    
    public void removed(@Nonnull ReadOnlyIndexedProperty<K, V, R> property, @Nonnull K oldKey, @Nonnull V oldValue);
    
}
