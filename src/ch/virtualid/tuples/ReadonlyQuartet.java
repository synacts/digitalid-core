package ch.virtualid.tuples;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Readonly;
import javax.annotation.Nonnull;

/**
 * This interface models a {@link Readonly readonly} quartet.
 * 
 * @see FreezableQuartet
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
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
