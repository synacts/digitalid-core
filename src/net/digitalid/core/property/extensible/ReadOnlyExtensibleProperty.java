package net.digitalid.core.property.extensible;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.collections.ReadOnlyCollection;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public interface ReadOnlyExtensibleProperty<E, R extends ReadOnlyCollection<E>> {
    
    public @Nonnull @NonFrozen R get();
    
}
