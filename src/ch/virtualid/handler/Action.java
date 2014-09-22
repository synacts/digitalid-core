package ch.virtualid.handler;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.packet.PacketException;
import ch.xdf.SignatureWrapper;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Actions affect the state of a virtual identity and are thus always {@link Audit audited}.
 * The default is to sign them identity-based. If another behavior is desired, the method
 * {@link Method#send()} needs to be overridden. Actions are executed asynchronously.
 * 
 * @see InternalAction
 * @see ExternalAction
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Action extends Method implements Auditable {
    
    /**
     * Creates an action that encodes the content of a packet for the given recipient about the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     */
    protected Action(@Nullable Entity entity, @Nonnull Identifier subject, @Nonnull HostIdentifier recipient) {
        super(entity, subject, recipient);
    }
    
    /**
     * Creates an action that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    protected Action(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) {
        super(entity, signature, recipient);
    }
    
    
    @Override
    public abstract @Nullable ActionReply executeOnHost() throws PacketException, SQLException;
    
    
    /**
     * Executes this internal action on the client.
     * 
     * @throws SQLException if this handler cannot be executed.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
    public abstract void executeOnClient() throws SQLException;
    
    
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
