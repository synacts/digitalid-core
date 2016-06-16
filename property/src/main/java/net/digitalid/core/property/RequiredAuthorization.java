package net.digitalid.core.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.concept.Concept;

/**
 * A state selector returns the SQL condition with which the returned state is restricted.
 * 
 * @see ConceptPropertyTable
 */
@Stateless
public abstract class RequiredAuthorization<C extends Concept<C, ?, ?>> {
    
    /**
     * Returns the SQL condition with which the returned state is restricted.
     * 
     * @param permissions the permissions that restrict the returned state.
     * @param restrictions the restrictions that restrict the returned state.
     * @param agent the agent whose authorization restricts the returned state.
     * 
     * @return the SQL condition with which the returned state is restricted.
     */
    @Pure
    public abstract @Nonnull String getStateFilter(@Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent);
    
    @Pure
    public abstract @Nonnull ReadOnlyAgentPermissions getRequiredPermissions(@Nonnull C concept);
    
    /**
     * Returns the agent that is required to execute the method.
     * 
     * @return the agent required to execute the method.
     */
    public @Nullable Agent getRequiredAgentToExecuteMethod(@Nonnull C concept) {
        return null;
    }
    
}
