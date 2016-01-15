package net.digitalid.service.core.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.service.core.action.synchronizer.Audit;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;

/**
 * Handlers that can be added to the {@link Audit} have to implement this interface.
 * 
 * @see Action
 * @see ActionReply
 */
@Immutable
public interface Auditable {
    
    /**
     * Returns the permission that an agent needs to cover in order to see the audit of this handler.
     * 
     * @return the permission that an agent needs to cover in order to see the audit of this handler.
     * 
     * @ensure return.areEmptyOrSingle() : "The returned permissions are empty or single.";
     */
    @Pure
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeAudit();
    
    /**
     * Returns the restrictions that an agent needs to cover in order to see the audit of this handler.
     * 
     * @return the restrictions that an agent needs to cover in order to see the audit of this handler.
     */
    @Pure
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit();
    
    /**
     * Returns the agent that an agent needs to cover in order to see the audit of this handler.
     * 
     * @return the agent that an agent needs to cover in order to see the audit of this handler.
     */
    @Pure
    public @Nullable Agent getRequiredAgentToSeeAudit();

}
