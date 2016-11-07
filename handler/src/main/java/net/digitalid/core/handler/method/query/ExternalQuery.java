package net.digitalid.core.handler.method.query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifier;

/**
 * External queries can be sent by both {@link Host hosts} and {@link Client clients}.
 */
@Immutable
public abstract class ExternalQuery extends Query {
    
    /**
     * Creates an external query that encodes the content of a packet for the given recipient about the given subject.
     * 
     * @param role the role to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     */
    protected ExternalQuery(@Nullable Role role, @Nonnull InternalIdentifier subject, @Nonnull HostIdentifier recipient) {
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
    protected ExternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException, InternalException {
        super(entity, signature, recipient);
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && other instanceof ExternalQuery;
    }
    
}
