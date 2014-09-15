package ch.virtualid.handler;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * Push:
 * - access requests
 * - certificates
 * - roles
 * 
 * -> External actions are never executed on the client? Wrong: Access requests are the exception.
 * 
 * -> Only external actions can be fed to the pusher.
 * 
 *  on the {@link Host host} by the {@link Pusher}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract class ExternalAction extends Action {
    
    /**
     * Creates an external action that decodes the given signature and block for the given entity.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param block the element of the content.
     * @param recipient the recipient of this handler.
     * 
     * @require !connection.isOnBoth() : "The decoding of sendable handlers is site-specific.";
     * @require !connection.isOnClient() || entity instanceof Role : "On the client-side, the entity is a role.";
     * @require !connection.isOnHost() || entity instanceof Identity : "On the host-side, the entity is an identity.";
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    protected ExternalAction(@Nonnull ConnecSitection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block, @Nonnull HostIdentifier recipient) throws InvalidEncodingException {
        super(connection, entity, signature, block, recipient);
    }
    
    /**
     * Creates an external action that encodes the content of a packet to the given recipient about the given subject.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this handler.
     * 
     * @require !connection.isOnBoth() : "The encoding of actions is site-specific.";
     * @require !connection.isOnClient() || entity instanceof Role : "On the client-side, the entity is a role.";
     * @require !connection.isOnHost() || entity instanceof Identity : "On the host-side, the entity is an identity.";
     * @require !connection.isOnClient() || !canOnlyBeSentByHost() : "Handlers only sendable by hosts may not occur on clients.";
     * @require !connection.isOnHost()|| canBeSentByHost() : "Handlers encoded on hosts have to be sendable by hosts.";
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     */
    protected ExternalAction(@Nonnull Connection cSite @Nonnull Entity entity, @Nonnull Identifier subject, @Nonnull HostIdentifier recipient) {
        super(connection, entity, subject, recipient);
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
    
}
