package net.digitalid.core.annotations.type;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Mutable;

/**
 * This interface is implemented by types, whose declaration can be loaded.
 */
@Mutable
public interface Loadable {
    
    /**
     * Returns whether the declaration of this type is loaded.
     */
    @Pure
    public boolean isLoaded();
    
}
