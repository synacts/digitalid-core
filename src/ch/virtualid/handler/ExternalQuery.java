package ch.virtualid.handler;

import ch.virtualid.contact.Authentications;
import ch.virtualid.entity.Entity;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * Examples:
 * - getAttributes
 * - getIdentity
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract class ExternalQuery extends Query {
    
    /**
     * Creates an external query that decodes the given signature and block for the given entity.
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
     */
    protected ExternalQuery(@Nonnull ConnecSitection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block, @Nonnull HostIdentifier recipient) throws InvalidEncodingException {
        super(connection, entity, signature, block, recipient);
    }
    
    /**
     * Creates an external query that encodes the content of a packet to the given recipient about the given subject.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this handler.
     * 
     * @require !connection.isOnBoth() || entity != null : "If the connection is site-specific, the entity is not null.";
     * @require !connection.isOnClient() || entity instanceof Role : "On the client-side, the entity is a role.";
     * @require !connection.isOnHost() || entity instanceof Identity : "On the host-side, the entity is an identity.";
     * @require !connection.isOnClient() || !canOnlyBeSentByHost() : "Handlers only sendable by hosts may not occur on clients.";
     * @require !connection.isOnHost()|| canBeSentByHost() : "Handlers encoded on hosts have to be sendable by hosts.";
     */
    protected ExternalQuery(@Nonnull Connection cSite @Nullable Entity entity, @Nonnull Identifier subject, @Nonnull HostIdentifier recipient) {
        super(connection, entity, subject, recipient);
    }
    
    
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && other instanceof ExternalQuery;
    }
    
    @Override
    public boolean canBeSentByHosts() {
        return false;
    }
    
    @Override
    public boolean canOnlyBeSentByHosts() {
        return false;
    }
    
    
    @Override
    public @Nonnull Authentications getDesiredAuthentications() {
        return Authentications.NONE;
    }
    
    
    public abstract @Nonnull Reply excecuteOnHost(@Nonnull HostSignatureWrapper hostSignature) throws PacketException;
    
}
