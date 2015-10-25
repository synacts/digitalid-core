package net.digitalid.service.core.property.extensible;

import javax.annotation.Nonnull;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.readonly.ReadOnlyCollection;

/**
 * Description.
 */
public interface ReadOnlyExtensibleProperty<E, R extends ReadOnlyCollection<E>> {
    
    public @Nonnull @NonFrozen R get();
    
}
