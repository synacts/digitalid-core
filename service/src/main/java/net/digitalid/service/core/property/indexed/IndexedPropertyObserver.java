package net.digitalid.service.core.property.indexed;

import javax.annotation.Nonnull;
import net.digitalid.service.core.property.PropertyObserver;
import net.digitalid.service.core.property.ReadOnlyProperty;
import net.digitalid.utility.collections.readonly.ReadOnlyMap;

/**
 * Objects that implement this interface can be used to observe {@link ReadOnlyIndexedProperty indexed properties}.
 */
public interface IndexedPropertyObserver<K, V, R extends ReadOnlyMap<K, V>> extends PropertyObserver {
    
    /**
     * This method is called on {@link ReadOnlyProperty#register(net.digitalid.service.core.property.PropertyObserver) registered} observers when an indexed value of the given property has been added.
     * 
     * @param property the property whose value has been added.
     * @param key the key of this property value that got added.
     * @param value the value of this property that got added.
     */
    public void added(@Nonnull ReadOnlyIndexedProperty<K, V, R> property, @Nonnull K newKey, @Nonnull V newValue);
    
    /**
     * This method is called on {@link ReadOnlyProperty#register(net.digitalid.service.core.property.PropertyObserver) registered} observers when an indexed value of the given property has been removed.
     * 
     * @param property the property whose value has been removed.
     * @param key the key of this property value that got removed.
     * @param value the value of this property that got removed.
     */
    public void removed(@Nonnull ReadOnlyIndexedProperty<K, V, R> property, @Nonnull K oldKey, @Nonnull V oldValue);
    
}
