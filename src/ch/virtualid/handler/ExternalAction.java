package ch.virtualid.handler;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.service.CoreServiceExternalAction;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.pusher.Pusher;
import ch.virtualid.host.Host;
import ch.xdf.SignatureWrapper;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * External actions can be sent by both {@link Host hosts} and {@link Client clients}.
 * Depending on whether the reply is needed immediately, external actions can be either sent 
 * directly or passed to the {@link Pusher#send(ch.virtualid.handler.ExternalAction) Pusher}!
 * 
 * @invariant hasEntity() : "This external action has an entity.";
 * 
 * @see CoreServiceExternalAction
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class ExternalAction extends Action {
    
    /**
     * Creates an external action that encodes the content of a packet for the given recipient about the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     */
    protected ExternalAction(@Nonnull NonHostEntity entity, @Nonnull InternalIdentifier subject, @Nonnull HostIdentifier recipient) {
        super(entity, subject, recipient);
    }
    
    /**
     * Creates an external action that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected ExternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) {
        super(entity, signature, recipient);
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && other instanceof ExternalAction;
    }
    
    
    @Pure
    @Override
    public boolean canBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public boolean canOnlyBeSentByHosts() {
        return false;
    }
    
    
    /**
     * Executes this action if an error occurred during pushing.
     */
    public abstract void executeOnFailure() throws SQLException;
    
    /**
     * Returns the permission that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     * 
     * @return the permission that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     * 
     * @ensure return.areEmptyOrSingle() : "The returned permissions are empty or single.";
     */
    @Pure
    public @Nonnull ReadonlyAgentPermissions getFailedAuditPermissions() {
        return AgentPermissions.NONE;
    }
    
    /**
     * Returns the restrictions that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     * 
     * @return the restrictions that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     */
    @Pure
    public @Nonnull Restrictions getFailedAuditRestrictions() {
        return Restrictions.MIN;
    }
    
    /**
     * Returns the agent that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     * 
     * @return the agent that an agent needs to cover in order to see the audit of this external action when the pushing failed.
     */
    @Pure
    public @Nullable Agent getFailedAuditAgent() throws SQLException {
        return null;
    }
    
}
