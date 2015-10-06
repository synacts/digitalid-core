package net.digitalid.core.property.extensible;

import javax.annotation.Nonnull;
import net.digitalid.collections.annotations.freezable.NonFrozen;
import net.digitalid.collections.readonly.ReadOnlyCollection;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public interface ReadOnlyExtensibleProperty<E, R extends ReadOnlyCollection<E>> {
    
    public @Nonnull @NonFrozen R get();
    
}
