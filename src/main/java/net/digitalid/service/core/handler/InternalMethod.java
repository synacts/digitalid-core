package net.digitalid.service.core.handler;

import net.digitalid.service.core.concepts.agent.Restrictions;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * Internal methods have to implement this interface in order to provide the required restrictions.
 * Additionally, this interface can also serve as a test whether some method is internal (and thus identity-based).
 * 
 * @see InternalAction
 * @see InternalQuery
 */
@Immutable
public interface InternalMethod {
    
    /**
     * Returns the restrictions required for this internal method.
     * 
     * @return the restrictions required for this internal method.
     */
    @Pure
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod();
    
}
