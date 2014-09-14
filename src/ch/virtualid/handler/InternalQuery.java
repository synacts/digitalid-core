package ch.virtualid.handler;

import ch.virtualid.contact.Authentications;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Entity;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.FailedIdentityException;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * Examples:
 * - getState
 * - getAgents
 * 
 * - Internal queries have to be signed by the client directly or with credentials in a role. -> Only true for the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract class InternalQuery extends Query implements InternalMethod {
    
    /**
     * Creates an internal query that decodes the given signature and block for the given entity.
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
     * @ensure getEntity() instanceof Identity : "The entity of this handler is an identity.";
     * @ensure getConnection().isOnHost() : "The connection of this handler is on the host-side.";
     * @ensure (!getSubject().getIdentity().equals(entity.getIdentity())) : "The identity of the entity and the subject are the same.";
     */
    protected InternalQuery(@Nonnull ConnecSitection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block, @Nonnull HostIdentifier recipient) throws InvalidEncodingException, FailedIdentityException {
        super(connection, entity, signature, block, recipient);
        
        if (!getSubject().getIdentity().equals(entity.getIdentity())) throw new InvalidEncodingException("The identity of the entity and the subject have to be the same for internal queries.");
    }
    
    /**
     * Creates an internal query that encodes the content of a packet to the given recipient about the given subject.
     * 
     * @param connection an open connection to the database.
     * @param role the role to which this handler belongs.
     * @param recipient the recipient of this handler.
     */
    protected InternalQuery(@Nonnull ClientClientSitection, @Nonnull Role role, @Nonnull HostIdentifier recipient) {
        super(connection, role, role.getIdentity().getAddress(), recipient);
    }
    
    
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && other instanceof InternalQuery;
    }
    
    @Override
    public final boolean canBeSentByHosts() {
        return false;
    }
    
    @Override
    public final boolean canOnlyBeSentByHosts() {
        return false;
    }
    
    
    /**
     * @ensure return.equals(Authentications.IDENTITY_BASED) : "Internal queries are always identity-based.";
     */
    @Override
    public final @Nonnull Authentications getDesiredAuthentications() {
        return Authentications.IDENTITY_BASED;
    }
    
}
