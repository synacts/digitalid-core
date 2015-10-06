package net.digitalid.service.core.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.agent.Agent;
import net.digitalid.service.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.agent.Restrictions;
import net.digitalid.utility.annotations.state.Pure;

/**
 * A state selector returns the SQL condition with which the returned state is restricted.
 * 
 * @see ConceptPropertyTable
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public interface StateSelector {
    
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
    public @Nonnull String getCondition(@Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent);
    
}
