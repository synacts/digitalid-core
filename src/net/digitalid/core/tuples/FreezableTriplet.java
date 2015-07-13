package net.digitalid.core.tuples;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Freezable;

/**
 * This class models a {@link Freezable freezable} triplet.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 2.0
 */
public class FreezableTriplet<E0, E1, E2> extends FreezablePair<E0, E1> implements ReadOnlyTriplet<E0, E1, E2> {
    
    /**
     * Stores the third element of this tuple.
     */
    private E2 element2;
    
    /**
     * Creates a new triplet with the given elements.
     * 
     * @param element0 the first element of this tuple.
     * @param element1 the second element of this tuple.
     * @param element2 the third element of this tuple.
     */
    public FreezableTriplet(E0 element0, E1 element1, E2 element2) {
        super(element0, element1);
        
        this.element2 = element2;
    }
    
    /**
     * Creates a new triplet from the given triplet.
     * 
     * @param triplet the triplet containing the elements.
     */
    public FreezableTriplet(@Nonnull ReadOnlyTriplet<E0, E1, E2> triplet) {
        super(triplet);
        
        this.element2 = triplet.getElement2();
    }
    
    
    @Pure
    @Override
    public final E2 getElement2() {
        return element2;
    }
    
    /**
     * Sets the third element of this tuple.
     * 
     * @param element2 the element to be set.
     * 
     * @require !isFrozen() : "This object is not frozen.";
     */
    public final void setElement2(E2 element2) {
        assert !isFrozen() : "This object is not frozen.";
        
        this.element2 = element2;
    }
    
    
    @Override
    public @Nonnull ReadOnlyTriplet<E0, E1, E2> freeze() {
        super.freeze();
        return this;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableTriplet<E0, E1, E2> clone() {
        return new FreezableTriplet<>(this);
    }
    
    
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
