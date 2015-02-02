package ch.virtualid.handler;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.synchronizer.Audit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Handlers that can be added to the {@link Audit} have to implement this interface.
 * 
 * @see Action
 * @see ActionReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface Auditable {
    
    /**
     * Returns the permission that an agent needs to cover in order to see the audit of this handler.
     * 
     * @return the permission that an agent needs to cover in order to see the audit of this handler.
     * 
     * @ensure return.areEmptyOrSingle() : "The returned permissions are empty or single.";
     */
    @Pure
    public @Nonnull ReadonlyAgentPermissions getAuditPermissions();
    
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
