package ch.virtualid.handler;

import ch.virtualid.agent.Restrictions;
import javax.annotation.Nonnull;

/**
 * Internal handlers have to implement this interface in order to provide the required restrictions.
 * Additionally, this interface can also serve as a test whether some handler is internal (and thus identity-based).
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface InternalHandler {
    
    /**
     * Returns the restrictions required for this handler.
     * 
     * @return the restrictions required for this handler.
     */
    public @Nonnull Restrictions getRequiredRestrictions();
    
}
