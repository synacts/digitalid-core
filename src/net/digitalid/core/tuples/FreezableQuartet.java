package net.digitalid.core.tuples;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.Freezable;

/**
 * This class models a {@link Freezable freezable} quartet.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 2.0
 */
public class FreezableQuartet<E0, E1, E2, E3> extends FreezableTriplet<E0, E1, E2> implements ReadOnlyQuartet<E0, E1, E2, E3> {
    
    /**
     * Stores the fourth element of this tuple.
     */
    private E3 element3;
    
    /**
     * Creates a new quartet with the given elements.
     * 
     * @param element0 the first element of this tuple.
     * @param element1 the second element of this tuple.
     * @param element2 the third element of this tuple.
     * @param element3 the fourth element of this tuple.
     */
    public FreezableQuartet(E0 element0, E1 element1, E2 element2, E3 element3) {
        super(element0, element1, element2);
        
        this.element3 = element3;
    }
    
    /**
     * Creates a new quartet from the given quartet.
     * 
     * @param quartet the quartet containing the elements.
     */
    public FreezableQuartet(@Nonnull ReadOnlyQuartet<E0, E1, E2, E3> quartet) {
        super(quartet);
        
        this.element3 = quartet.getElement3();
    }
    
    
    @Pure
    @Override
    public final E3 getElement3() {
        return element3;
    }
    
    /**
     * Sets the fourth element of this tuple.
     * 
     * @param element3 the element to be set.
     * 
     * @require !isFrozen() : "This object is not frozen.";
     */
    public final void setElement3(E3 element3) {
        assert !isFrozen() : "This object is not frozen.";
        
        this.element3 = element3;
    }
    
    
    @Override
    public @Nonnull ReadOnlyQuartet<E0, E1, E2, E3> freeze() {
        super.freeze();
        return this;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableQuartet<E0, E1, E2, E3> clone() {
        return new FreezableQuartet<>(this);
    }
    
    
    @Pure
    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null) return false;
        if (!(object instanceof FreezableQuartet)) return object.equals(this);
        final @Nonnull FreezableQuartet other = (FreezableQuartet) object;
        return super.equals(object) && Objects.equals(this.element3, other.element3);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 83 * super.hashCode() + Objects.hashCode(element3);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return super.toString() + ", " + element3;
    }
    
}
