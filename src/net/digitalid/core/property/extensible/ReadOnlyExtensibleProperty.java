package net.digitalid.core.property.extensible;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.collections.ReadonlyCollection;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public interface ReadOnlyExtensibleProperty<E, R extends ReadonlyCollection<E>> {
    
    public @Nonnull @NonFrozen R get();
    
}
