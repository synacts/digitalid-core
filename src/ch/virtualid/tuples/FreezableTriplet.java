package ch.virtualid.tuples;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import javax.annotation.Nonnull;

/**
 * This class models a {@link Freezable freezable} triplet.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public class FreezableTriplet<E0, E1, E2> extends FreezablePair<E0, E1> implements ReadonlyTriplet<E0, E1, E2> {
    
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
    public FreezableTriplet(@Nonnull ReadonlyTriplet<E0, E1, E2> triplet) {
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
     * @require isNotFrozen() : "This object is not frozen.";
     */
    public final void setElement2(E2 element2) {
        assert isNotFrozen() : "This object is not frozen.";
        
        this.element2 = element2;
    }
    
    @Override
    public @Nonnull ReadonlyTriplet<E0, E1, E2> freeze() {
        super.freeze();
        return this;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableTriplet<E0, E1, E2> clone() {
        return new FreezableTriplet<E0, E1, E2>(this);
    }
    
}
