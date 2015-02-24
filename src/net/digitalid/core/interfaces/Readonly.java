package net.digitalid.core.interfaces;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;

/**
 * Interfaces that extend this interface provide readonly access to their objects.
 * When a readonly object is {@link Object#clone() cloned}, its copy is of the corresponding {@link Freezable freezable} type and {@link #isNotFrozen() not frozen}.
 * 
 * @see Freezable
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface Readonly extends Cloneable {
    
    /**
     * Returns whether this object is frozen and can thus no longer be modified.
     * 
     * @return whether this object is frozen and can thus no longer be modified.
     * 
     * @ensure !old(isFrozen()) || isFrozen() : "Once frozen, this object remains frozen.";
     */
    @Pure
    public boolean isFrozen();
    
    /**
     * Returns whether this object is not frozen and can thus still be modified.
     * 
     * @return whether this object is not frozen and can thus still be modified.
     * 
     * @ensure isNotFrozen() != isFrozen() : "An object is either frozen or not.";
     */
    @Pure
    public boolean isNotFrozen();
    
    
    /**
     * @ensure clone.isNotFrozen() : "The clone is not frozen.";
     */
    @Pure
    public @Capturable @Nonnull Freezable clone();
    
}
