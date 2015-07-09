package net.digitalid.core.tuples;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.ReadOnly;

/**
 * This interface models a {@link ReadOnly read-only} triplet.
 * 
 * @see FreezableTriplet
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyTriplet<E0, E1, E2> extends ReadOnlyPair<E0, E1> {
    
    /**
     * Returns the third element of this tuple.
     * 
     * @return the third element of this tuple.
     */
    @Pure
    public E2 getElement2();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableTriplet<E0, E1, E2> clone();
    
}
