package ch.virtualid.handler;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.handler.query.external.CoreServiceExternalQuery;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.xdf.SignatureWrapper;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * External queries can be sent by both {@link Host hosts} and {@link Client clients}.
 * 
 * @see CoreServiceExternalQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class ExternalQuery extends Query {
    
    /**
     * Creates an external query that encodes the content of a packet for the given recipient about the given subject.
     * 
     * @param role the role to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     */
    protected ExternalQuery(@Nullable Role role, @Nonnull Identifier subject, @Nonnull HostIdentifier recipient) {
        super(role, subject, recipient);
    }
    
    /**
     * Creates an external query that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasEntity() : "This method has an entity.";
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    protected ExternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException {
        super(entity, signature, recipient);
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && other instanceof ExternalQuery;
    }
    
}
