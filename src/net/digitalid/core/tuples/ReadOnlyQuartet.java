package net.digitalid.core.tuples;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Readonly;

/**
 * This interface models a {@link Readonly readonly} quartet.
 * 
 * @see FreezableQuartet
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadonlyQuartet<E0, E1, E2, E3> extends ReadonlyTriplet<E0, E1, E2> {
    
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
