package net.digitalid.core.collections;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;

/**
 * Interfaces that extend this interface provide read-only access to their objects.
 * When a read-only object is {@link Object#clone() cloned}, its copy is of the corresponding {@link Freezable freezable} type and not {@link #isFrozen() frozen}.
 * 
 * @see Freezable
 * @see ReadOnlyIterable
 * @see ReadOnlyIterator
 * @see ReadOnlyMap
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnly extends Cloneable {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Freezable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns whether this object is frozen and can thus no longer be modified.
     * 
     * @return whether this object is frozen and can thus no longer be modified.
     * 
     * @ensure !old(isFrozen()) || isFrozen() : "Once frozen, this object remains frozen.";
     */
    @Pure
    public boolean isFrozen();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    public @Capturable @Nonnull @NonFrozen Freezable clone();
    
}
