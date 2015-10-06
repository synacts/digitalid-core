package net.digitalid.core.handler;

import javax.annotation.Nonnull;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.annotations.state.Pure;

/**
 * Internal methods have to implement this interface in order to provide the required restrictions.
 * Additionally, this interface can also serve as a test whether some method is internal (and thus identity-based).
 * 
 * @see InternalAction
 * @see InternalQuery
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public interface InternalMethod {
    
    /**
     * Returns the restrictions required for this internal method.
     * 
     * @return the restrictions required for this internal method.
     */
    @Pure
    public @Nonnull Restrictions getRequiredRestrictions();
    
}
