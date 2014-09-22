package ch.virtualid.handler.query.external;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.handler.ExternalQuery;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the {@link ExternalQuery external queries} of the {@link CoreService core service}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class CoreServiceExternalQuery extends ExternalQuery {
    
    /**
     * Creates an external query of the core service that decodes the given signature and block for the given entity.
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
    protected CoreServiceExternalQuery(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block, @Nonnull HostIdentifier recipient) throws InvalidEncodingException {
        super(connection, entity, signature, block, recipient);
    }
    
    /**
     * Creates an external query of the core service that encodes the content of a packet to the given recipient about the given subject.
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
    protected CoreServiceExternalQuery(@Nonnull Entity connection, @Nullable Entity entity, @Nonnull Identifier subject, @Nonnull HostIdentifier recipient) {
        super(connection, entity, subject, recipient);
    }
    
    
    @Pure
    @Override
    public final @Nonnull SemanticType getService() {
        return CoreService.TYPE;
    }
    
    
}
