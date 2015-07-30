package net.digitalid.core.tuples;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnly;

/**
 * This interface models a {@link ReadOnly read-only} triplet.
 * 
 * @see FreezableTriplet
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyTriplet<E0, E1, E2> extends ReadOnlyPair<E0, E1> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Getter –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the third element of this tuple.
     * 
     * @return the third element of this tuple.
     */
    @Pure
    public @Nullable E2 getElement2();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableTriplet<E0, E1, E2> clone();
    
}
