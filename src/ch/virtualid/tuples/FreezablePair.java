package ch.virtualid.tuples;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Freezable;
import ch.virtualid.interfaces.FreezableObject;
import javax.annotation.Nonnull;

/**
 * This class models a {@link Freezable freezable} pair.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public class FreezablePair<E0, E1> extends FreezableObject implements ReadonlyPair<E0, E1> {
    
    /**
     * Stores the first element of this tuple.
     */
    private E0 element0;
    
    /**
     * Stores the second element of this tuple.
     */
    private E1 element1;
    
    /**
     * Creates a new pair with the given elements.
     * 
     * @param element0 the first element of this tuple.
     * @param element1 the second element of this tuple.
     */
    public FreezablePair(E0 element0, E1 element1) {
        this.element0 = element0;
        this.element1 = element1;
    }
    
    /**
     * Creates a new pair from the given pair.
     * 
     * @param pair the pair containing the elements.
     */
    public FreezablePair(@Nonnull ReadonlyPair<E0, E1> pair) {
        this.element0 = pair.getElement0();
        this.element1 = pair.getElement1();
    }
    
    @Pure
    @Override
    public final E0 getElement0() {
        return element0;
    }
    
    @Pure
    @Override
    public final E1 getElement1() {
        return element1;
    }
    
    /**
     * Sets the first element of this tuple.
     * 
     * @param element0 the element to be set.
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     */
    public final void setElement0(E0 element0) {
        assert isNotFrozen() : "This object is not frozen.";
        
        this.element0 = element0;
    }
    
    /**
     * Sets the second element of this tuple.
     * 
     * @param element1 the element to be set.
     * 
     * @require isNotFrozen() : "This object is not frozen.";
     */
    public final void setElement1(E1 element1) {
        assert isNotFrozen() : "This object is not frozen.";
        
        this.element1 = element1;
    }
    
    @Override
    public @Nonnull ReadonlyPair<E0, E1> freeze() {
        super.freeze();
        return this;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezablePair<E0, E1> clone() {
        return new FreezablePair<E0, E1>(this);
    }
    
}
