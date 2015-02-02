package ch.virtualid.handler;

import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import javax.annotation.Nonnull;

/**
 * Internal methods have to implement this interface in order to provide the required restrictions.
 * Additionally, this interface can also serve as a test whether some method is internal (and thus identity-based).
 * 
 * @see InternalAction
 * @see InternalQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface InternalMethod {
    
    /**
     * Returns the restrictions required for this internal method.
     * 
     * @return the restrictions required for this internal method.
     */
    @Pure
    public @Nonnull Restrictions getRequiredRestrictions();
    
}
