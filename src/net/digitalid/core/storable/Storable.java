package net.digitalid.core.storable;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.wrappers.Block;

/**
 * Objects of classes that implement this interface can be stored as a {@link Block block} or in the {@link Database database}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface Storable<V> {
    
    /**
     * 
     * 
     * @return 
     */
    @Pure
    public @Nonnull Factory<V> getFactory();
    
}
