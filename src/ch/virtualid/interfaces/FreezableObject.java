package ch.virtualid.interfaces;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import javax.annotation.Nonnull;

/**
 * This class implements the freezing mechanism which can be reused with inheritance.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class FreezableObject implements Freezable {
    
    /**
     * Stores whether this object is frozen.
     */
    private boolean frozen = false;
    
    @Pure
    @Override
    public final boolean isFrozen() {
        return frozen;
    }
    
    @Pure
    @Override
    public final boolean isNotFrozen() {
        return !frozen;
    }
    
    @Override
    public @Nonnull Readonly freeze() {
        frozen = true;
        return this;
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableObject clone() {
        return new FreezableObject();
    }
    
}
