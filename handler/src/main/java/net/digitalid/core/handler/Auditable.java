package net.digitalid.core.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.synchronizer.Audit;

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
