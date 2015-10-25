package net.digitalid.service.core.property.extensible;

import javax.annotation.Nonnull;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableCollection;
import net.digitalid.utility.collections.readonly.ReadOnlyCollection;

/**
 * Description.
 */
public class ExtensibleProperty<E, R extends ReadOnlyCollection<E>, F extends FreezableCollection<E>> implements ReadOnlyExtensibleProperty<E, R> {
    
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
