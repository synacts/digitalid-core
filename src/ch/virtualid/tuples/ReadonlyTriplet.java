package ch.virtualid.tuples;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Readonly;
import javax.annotation.Nonnull;

/**
 * This interface models a {@link Readonly readonly} triplet.
 * 
 * @see FreezableTriplet
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyTriplet<E0, E1, E2> extends ReadonlyPair<E0, E1> {
    
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
