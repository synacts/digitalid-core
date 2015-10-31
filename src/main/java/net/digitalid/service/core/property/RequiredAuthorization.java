package net.digitalid.service.core.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;

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
    public abstract @Nonnull ReadOnlyAgentPermissions getRequiredPermissions(C concept);
    
    /**
     * Returns the agent that is required to execute the method.
     * 
     * @return the agent required to execute the method.
     */
    public @Nullable Agent getRequiredAgentToExecuteMethod() {
        return null;
    }
}
