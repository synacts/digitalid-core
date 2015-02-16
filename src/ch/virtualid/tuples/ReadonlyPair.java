package ch.virtualid.tuples;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Readonly;
import javax.annotation.Nonnull;

/**
 * This interface models a {@link Readonly readonly} pair.
 * 
 * @see FreezablePair
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyPair<E0, E1> extends Readonly {
    
    /**
     * Returns the first element of this tuple.
     * 
     * @return the first element of this tuple.
     */
    @Pure
    public E0 getElement0();
    
    /**
     * Returns the second element of this tuple.
     * 
     * @return the second element of this tuple.
     */
    @Pure
    public E1 getElement1();
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezablePair<E0, E1> clone();
    
}
