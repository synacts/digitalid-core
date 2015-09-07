package net.digitalid.core.auxiliary;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Stateless;

/**
 * This class is an alternative to {@link Void} which supports non-nullable parameters and return values.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Stateless
public final class None {
    
    /**
     * Creates a new none.
     */
    private None() {}
    
    /**
     * Stores the only object of this class.
     */
    public static final @Nonnull None OBJECT = new None();
    
}
