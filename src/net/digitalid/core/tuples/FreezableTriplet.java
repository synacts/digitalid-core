package net.digitalid.core.tuples;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonFrozenRecipient;
import net.digitalid.core.annotations.NullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.Freezable;

/**
 * This class models a {@link Freezable freezable} triplet.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class FreezableTriplet<E0, E1, E2> extends FreezablePair<E0, E1> implements ReadOnlyTriplet<E0, E1, E2> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Field –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the third element of this tuple.
     */
    private @Nullable E2 element2;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new triplet with the given elements.
     * 
     * @param element0 the first element of this tuple.
     * @param element1 the second element of this tuple.
     * @param element2 the third element of this tuple.
     */
    protected FreezableTriplet(@Nullable E0 element0, @Nullable E1 element1, @Nullable E2 element2) {
        super(element0, element1);
        
        this.element2 = element2;
    }
    
    /**
     * Creates a new triplet with the given elements.
     * 
     * @param element0 the first element of this tuple.
     * @param element1 the second element of this tuple.
     * @param element2 the third element of this tuple.
     * 
     * @return a new triplet with the given elements.
     */
    @Pure
    public static @Capturable @Nonnull @NonFrozen <E0, E1, E2> FreezableTriplet<E0, E1, E2> get(@Nullable E0 element0, @Nullable E1 element1, @Nullable E2 element2) {
        return new FreezableTriplet<>(element0, element1, element2);
    }
    
    /**
     * Creates a new triplet from the given triplet.
     * 
     * @param triplet the triplet containing the elements.
     * 
     * @return a new triplet from the given triplet.
     */
    @Pure
    public static @Capturable @Nonnull @NonFrozen <E0, E1, E2> FreezableTriplet<E0, E1, E2> getNonNullable(@Nonnull @NullableElements ReadOnlyTriplet<E0, E1, E2> triplet) {
        return get(triplet.getElement0(), triplet.getElement1(), triplet.getElement2());
    }
    
    /**
     * Creates a new triplet from the given triplet.
     * 
     * @param triplet the triplet containing the elements.
     * 
     * @return a new triplet from the given triplet.
     */
    @Pure
    public static @Capturable @Nullable @NonFrozen <E0, E1, E2> FreezableTriplet<E0, E1, E2> getNullable(@Nullable @NullableElements ReadOnlyTriplet<E0, E1, E2> triplet) {
        return triplet == null ? null : getNonNullable(triplet);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Getter –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Nullable E2 getElement2() {
        return element2;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Setter –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Sets the third element of this tuple.
     * 
     * @param element2 the element to be set.
     */
    @NonFrozenRecipient
    public final void setElement2(@Nullable E2 element2) {
        assert !isFrozen() : "This object is not frozen.";
        
        this.element2 = element2;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Freezable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    public @Nonnull @Frozen ReadOnlyTriplet<E0, E1, E2> freeze() {
        super.freeze();
        return this;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableTriplet<E0, E1, E2> clone() {
        return FreezableTriplet.getNonNullable(this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null) return false;
        if (!(object instanceof FreezableTriplet)) return object.equals(this);
        final @Nonnull FreezableTriplet other = (FreezableTriplet) object;
        return super.equals(object) && Objects.equals(this.element2, other.element2);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 83 * super.hashCode() + Objects.hashCode(element2);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return super.toString() + ", " + element2;
    }
    
}
