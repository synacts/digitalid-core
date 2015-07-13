package net.digitalid.core.tuples;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnly;

/**
 * This interface models a {@link ReadOnly read-only} quartet.
 * 
 * @see FreezableQuartet
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyQuartet<E0, E1, E2, E3> extends ReadOnlyTriplet<E0, E1, E2> {
    
    /**
     * Returns the third element of this tuple.
     * 
     * @return the third element of this tuple.
     */
    @Pure
    public E3 getElement3();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableQuartet<E0, E1, E2, E3> clone();
    
}
