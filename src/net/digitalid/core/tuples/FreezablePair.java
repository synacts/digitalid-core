package net.digitalid.core.tuples;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.interfaces.Freezable;
import net.digitalid.core.interfaces.FreezableObject;

/**
 * This class models a {@link Freezable freezable} pair.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 2.0
 */
public class FreezablePair<E0, E1> extends FreezableObject implements ReadOnlyPair<E0, E1> {
    
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
    public FreezablePair(@Nonnull ReadOnlyPair<E0, E1> pair) {
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
    public @Nonnull ReadOnlyPair<E0, E1> freeze() {
        super.freeze();
        return this;
    }
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezablePair<E0, E1> clone() {
        return new FreezablePair<>(this);
    }
    
    
    @Pure
    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null) return false;
        if (!(object instanceof FreezablePair)) return object.equals(this);
        final @Nonnull FreezablePair other = (FreezablePair) object;
        return Objects.equals(this.element0, other.element0) && Objects.equals(this.element1, other.element1);
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(element0);
        hash = 83 * hash + Objects.hashCode(element1);
        return hash;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return element0 + ", " + element1;
    }
    
}
