package ch.virtualid.handler;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.handler.query.external.CoreServiceExternalQuery;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identity;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
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
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     */
    protected ExternalQuery(@Nullable Entity entity, @Nonnull Identity subject, @Nonnull HostIdentifier recipient) {
        super(entity, subject.getAddress(), recipient);
    }
    
    /**
     * Creates an external query that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     * @ensure getSignature() != null : "The signature of this handler is not null.";
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
