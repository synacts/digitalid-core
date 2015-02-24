package net.digitalid.core.interfaces;

import javax.annotation.Nonnull;

/**
 * Classes that implement this interface allow their objects to transition from a mutable into an {@link Immutable immutable} state.
 * 
 * @see FreezableObject
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface Freezable extends Readonly {
    
    /**
     * Freezes this object and thus makes it immutable.
     * Make sure to overwrite this method and freeze all mutable fields!
     * 
     * @return a reference to this object in order to allow chaining.
     * 
     * @ensure isFrozen() : "This object is now frozen.";
     */
    public @Nonnull Readonly freeze();
    
}
