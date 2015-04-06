package net.digitalid.core.property.extensible;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.collections.FreezableCollection;
import net.digitalid.core.collections.ReadonlyCollection;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class ExtensibleProperty<E, R extends ReadonlyCollection<E>, F extends FreezableCollection<E>> implements ReadOnlyExtensibleProperty<E, R> {
    
    @Override
    public @NonFrozen R get() {
        throw new UnsupportedOperationException("get in ExtensibleProperty is not supported yet.");
    }
    
    public void add(@Nonnull @Frozen R elements) {
        // TODO
    }
    
    public void remove(@Nonnull @Frozen R elements) {
        // TODO
    }
    
}
