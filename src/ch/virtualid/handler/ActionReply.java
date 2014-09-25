package ch.virtualid.handler;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.handler.reply.action.CoreServiceActionReply;
import ch.virtualid.module.Service;
import ch.virtualid.packet.Audit;
import ch.virtualid.pusher.Pusher;
import ch.xdf.HostSignatureWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a {@link Reply reply} to an {@link ExternalAction external action}.
 * Action replies are added to the {@link Audit audit} by the {@link Pusher pusher} on {@link Service services}.
 * 
 * @see CoreServiceActionReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class ActionReply extends Reply implements Auditable {
    
    /**
     * Creates an action reply that encodes the content of a packet.
     * 
     * @param account the account to which this action reply belongs.
     */
    protected ActionReply(@Nonnull Account account) {
        super(account);
    }
    
    /**
     * Creates an action reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    protected ActionReply(@Nullable Entity entity, @Nonnull HostSignatureWrapper signature, long number) {
        super(entity, signature, number);
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getAuditPermissions() {
        return AgentPermissions.NONE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return Restrictions.NONE;
    }
    
    @Pure
    @Override
    public @Nullable Agent getAuditAgent() {
        return null;
    }
    
}
