package ch.virtualid.util;

import ch.virtualid.annotations.Pure;
import java.util.Set;

/**
 * Extends Java's {@link java.util.Set} interface.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
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
