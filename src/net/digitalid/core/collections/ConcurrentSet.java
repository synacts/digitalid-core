package net.digitalid.core.collections;

import java.util.Set;
import net.digitalid.core.annotations.Pure;

/**
 * Extends Java's {@link java.util.Set} interface.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ConcurrentSet<E> extends Set<E> {
    
    /**
     * Returns whether this set is not empty.
     * 
     * @return whether this set is not empty.
     */
    @Pure
    public boolean isNotEmpty();
    
}
