package net.digitalid.core.storable;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;

/**
 * Objects of classes that implement this interface are stored as a single (non-blob) column in the {@link Database database}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface SingleColumnStorable<O> extends Storable<O> {
    
    /**
     * Returns the string that represents this object in the database.
     * 
     * @return the string that represents this object in the database.
     */
    @Pure
    @Override
    public @Nonnull String toString();
    
}
