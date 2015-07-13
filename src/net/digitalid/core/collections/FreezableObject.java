package net.digitalid.core.collections;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;

/**
 * This class implements the freezing mechanism which can be reused with inheritance.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
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
    
    @Override
    public @Nonnull ReadOnly freeze() {
        frozen = true;
        return this;
    }
    
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableObject clone() {
        return new FreezableObject();
    }
    
}
