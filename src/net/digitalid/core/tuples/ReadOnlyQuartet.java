package net.digitalid.core.tuples;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonFrozen;
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Getter –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the third element of this tuple.
     * 
     * @return the third element of this tuple.
     */
    @Pure
    public @Nullable E3 getNullableElement3();
    
    /**
     * Returns the third element of this tuple.
     * 
     * @return the third element of this tuple.
     */
    @Pure
    public @Nonnull E3 getNonNullableElement3();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableQuartet<E0, E1, E2, E3> clone();
    
}
