package net.digitalid.core.property.extensible;

import javax.annotation.Nonnull;
import net.digitalid.collections.annotations.freezable.Frozen;
import net.digitalid.collections.readonly.ReadOnlyCollection;
import net.digitalid.core.property.PropertyObserver;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public interface ExtensiblePropertyObserver<E, R extends ReadOnlyCollection<E>> extends PropertyObserver {
    
    public void added(@Nonnull ReadOnlyExtensibleProperty<E, R> property, @Nonnull @Frozen R newElements);
    
    public void removed(@Nonnull ReadOnlyExtensibleProperty<E, R> property, @Nonnull @Frozen R oldElements);
    
}
