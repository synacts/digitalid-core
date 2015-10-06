package net.digitalid.service.core.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.agent.Agent;
import net.digitalid.service.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.agent.Restrictions;
import net.digitalid.service.core.synchronizer.Audit;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * Handlers that can be added to the {@link Audit} have to implement this interface.
 * 
 * @see Action
 * @see ActionReply
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
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
    public @Nonnull ReadOnlyAgentPermissions getAuditPermissions();
    
    /**
     * Returns the restrictions that an agent needs to cover in order to see the audit of this handler.
     * 
     * @return the restrictions that an agent needs to cover in order to see the audit of this handler.
     */
    @Pure
    public @Nonnull Restrictions getAuditRestrictions();
    
    /**
     * Returns the agent that an agent needs to cover in order to see the audit of this handler.
     * 
     * @return the agent that an agent needs to cover in order to see the audit of this handler.
     */
    @Pure
    public @Nullable Agent getAuditAgent();

}
