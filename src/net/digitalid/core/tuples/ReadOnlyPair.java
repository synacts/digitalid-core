package net.digitalid.core.tuples;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Readonly;

/**
 * This interface models a {@link Readonly readonly} pair.
 * 
 * @see FreezablePair
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
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
