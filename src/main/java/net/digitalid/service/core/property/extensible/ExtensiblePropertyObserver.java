package net.digitalid.service.core.property.extensible;

import javax.annotation.Nonnull;
import net.digitalid.service.core.property.PropertyObserver;
import net.digitalid.service.core.property.ReadOnlyProperty;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.readonly.ReadOnlySet;

/**
 * Objects that implement this interface can be used to observe {@link ReadOnlyExtensibleProperty extensible properties}.
 */
public interface ExtensiblePropertyObserver<E, R extends ReadOnlySet<E>> extends PropertyObserver {
    
    /**
     * This method is called on {@link ReadOnlyProperty#register(net.digitalid.service.core.property.PropertyObserver) registered} observers when an element of the given extensible property has been added.
     * 
     * @param property the property whose element has been added.
     * @param element the element of this property that got added.
     */
    public void added(@Nonnull ReadOnlyExtensibleProperty<E, R> property, @Nonnull @Frozen E element);
    
    /**
     * This method is called on {@link ReadOnlyProperty#register(net.digitalid.service.core.property.PropertyObserver) registered} observers when an element of the given extensible property has been removed.
     * 
     * @param property the property whose element has been removed.
     * @param element the element of this property that got removed.
     */
    public void removed(@Nonnull ReadOnlyExtensibleProperty<E, R> property, @Nonnull @Frozen E element);
    
}
