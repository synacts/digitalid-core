package net.digitalid.core.tuples;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnly;

/**
 * This interface models a {@link ReadOnly read-only} pair.
 * 
 * @see FreezablePair
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyPair<E0, E1> extends ReadOnly {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Getters –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the first element of this tuple.
     * 
     * @return the first element of this tuple.
     */
    @Pure
    public @Nullable E0 getNullableElement0();
    
    /**
     * Returns the first element of this tuple.
     * 
     * @return the first element of this tuple.
     */
    @Pure
    public @Nonnull E0 getNonNullableElement0();
    
    /**
     * Returns the second element of this tuple.
     * 
     * @return the second element of this tuple.
     */
    @Pure
    public @Nullable E1 getNullableElement1();
    
    /**
     * Returns the second element of this tuple.
     * 
     * @return the second element of this tuple.
     */
    @Pure
    public @Nonnull E1 getNonNullableElement1();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezablePair<E0, E1> clone();
    
}
