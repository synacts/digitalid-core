package ch.virtualid.handler;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.reply.action.CoreServiceActionReply;
import ch.virtualid.module.Service;
import ch.virtualid.packet.Audit;
import ch.virtualid.pusher.Pusher;
import ch.xdf.HostSignatureWrapper;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a {@link Reply reply} to an {@link ExternalAction external action}.
 * Action replies are added to the {@link Audit audit} by the {@link Pusher pusher} on {@link Service services}.
 * 
 * @invariant hasEntity() : "This action reply has an entity.");
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
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected ActionReply(@Nullable Entity entity, @Nonnull HostSignatureWrapper signature, long number) throws InvalidEncodingException {
        super(entity, signature, number);
        
        if (!hasEntity()) throw new InvalidEncodingException("An action reply must have an entity.");
    }
    
    
    /**
     * Returns the class of the external action whose reply class this is.
     * 
     * @return the class of the external action whose reply class this is.
     */
    @Pure
    @Deprecated // TODO: Was probably replaced with the getReplyClass() in the method.
    public abstract @Nonnull Class<? extends ExternalAction> getActionClass();
    
    
    /**
     * Executes this action reply by the pusher.
     * 
     * @param action the external action that was sent.
     * 
     * @throws PacketException if the authorization is not sufficient.
     * 
     * @require hasSignature() : "This handler has a signature.";
     * @require getActionClass().isInstance(action) : "The given action is an instance of the indicated class.";
     * @require getSubject().equals(action.getSubject()) : "The subjects of the reply and the action are the same.";
     * @require getEntityNotNull().equals(action.getEntityNotNull()) : "The entities of the reply and the action are the same.";
     * @require ((HostSignatureWrapper) getSignatureNotNull()).getSigner().equals(action.getRecipient()) : "The reply is signed by the action's recipient.";
     */
    public abstract void executeByPusher(@Nonnull ExternalAction action) throws PacketException, SQLException;
    
    /**
     * Executes this action reply by the synchronizer.
     * 
     * @throws SQLException if this handler cannot be executed.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
    public abstract void executeBySynchronizer() throws SQLException;
    
    
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
