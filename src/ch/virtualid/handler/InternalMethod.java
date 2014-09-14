package ch.virtualid.handler;

import ch.virtualid.agent.Restrictions;
import javax.annotation.Nonnull;

/**
 * Internal methods have to implement this interface in order to provide the required restrictions.
 * Additionally, this interface can also serve as a test whether some method is internal (and thus identity-based).
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface InternalMethod {
    
    /**
     * Returns the restrictions required for this method.
     * 
     * @return the restrictions required for this method.
     */
    public @Nonnull Restrictions getRequiredRestrictions();
    
}
