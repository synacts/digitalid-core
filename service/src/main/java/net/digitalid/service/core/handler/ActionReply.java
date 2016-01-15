package net.digitalid.service.core.handler;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.internal.InternalException;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.service.core.action.pusher.Pusher;
import net.digitalid.service.core.action.synchronizer.Audit;
import net.digitalid.service.core.block.wrappers.signature.HostSignatureWrapper;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.entity.Account;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.core.CoreServiceActionReply;
import net.digitalid.service.core.storage.Service;

/**
 * This class models a {@link Reply reply} to an {@link ExternalAction external action}.
 * Action replies are added to the {@link Audit audit} by the {@link Pusher pusher} on {@link Service services}.
 * 
 * @invariant hasEntity() : "This action reply has an entity.");
 * 
 * @see CoreServiceActionReply
 */
@Immutable
public abstract class ActionReply extends Reply implements Auditable {
    
    /**
     * Creates an action reply that encodes the content of a packet.
     * 
     * @param account the account to which this action reply belongs.
     */
    protected ActionReply(@Nonnull Account account) {
        super(account, account.getIdentity().getAddress());
    }
    
    /**
     * Creates an action reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected ActionReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number) throws InternalException {
        super(entity, signature, number);
        
        if (!hasEntity()) { throw InternalException.get("An action reply must have an entity."); }
    }
    
    
    /**
     * Executes this action reply by the pusher.
     * 
     * @param action the external action that was sent.
     * 
     * @throws RequestException if the authorization is not sufficient.
     * 
     * @require hasSignature() : "This handler has a signature.";
     * @require action.getReplyClass().isInstance(this) : "This object is an instance of the action's reply class.";
     * @require getSubject().equals(action.getSubject()) : "The subjects of the reply and the action are the same.";
     * @require getEntityNotNull().equals(action.getEntityNotNull()) : "The entities of the reply and the action are the same.";
     * @require ((HostSignatureWrapper) getSignatureNotNull()).getSigner().equals(action.getRecipient()) : "The reply is signed by the action's recipient.";
     */
    @NonCommitting
    public abstract void executeByPusher(@Nonnull ExternalAction action) throws RequestException, SQLException;
    
    /**
     * Executes this action reply by the synchronizer.
     * 
     * @throws DatabaseException if this handler cannot be executed.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
    @NonCommitting
    public abstract void executeBySynchronizer() throws DatabaseException;
    
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeAudit() {
        return FreezableAgentPermissions.NONE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
        return Restrictions.MIN;
    }
    
    @Pure
    @Override
    public @Nullable Agent getRequiredAgentToSeeAudit() {
        return null;
    }
    
}
