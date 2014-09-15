package ch.virtualid.handler;

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
 * {@link Method#send()} needs to be overridden.
 * 
 * @see InternalAction
 * @see ExternalAction
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Action extends Method {
    
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
    public abstract @Nullable ActionReply excecute() throws PacketException, SQLException;
    
    
    /**
     * Returns the permission that an agent needs to cover in order to see the audit of this action.
     * 
     * @return the permission that an agent needs to cover in order to see the audit of this action.
     * 
     * @ensure return.areSingle() : "The result is a single permission.";
     */
    @Pure
    public abstract @Nonnull ReadonlyAgentPermissions getAuditPermissions();
    
    /**
     * Returns the restrictions that an agent needs to cover in order to see the audit of this action.
     * 
     * @return the restrictions that an agent needs to cover in order to see the audit of this action.
     */
    @Pure
    public abstract @Nonnull Restrictions getAuditRestrictions();
    
}
