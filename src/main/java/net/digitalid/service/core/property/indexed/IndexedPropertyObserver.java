package net.digitalid.service.core.property.indexed;

import javax.annotation.Nonnull;
import net.digitalid.service.core.property.PropertyObserver;
import net.digitalid.utility.collections.readonly.ReadOnlyMap;

/**
 * Description.
 */
public interface IndexedPropertyObserver<K, V, R extends ReadOnlyMap<K, V>> extends PropertyObserver {
    
    public void added(@Nonnull ReadOnlyIndexedProperty<K, V, R> property, @Nonnull K newKey, @Nonnull V newValue);
    
    public void removed(@Nonnull ReadOnlyIndexedProperty<K, V, R> property, @Nonnull K oldKey, @Nonnull V oldValue);
    
}
