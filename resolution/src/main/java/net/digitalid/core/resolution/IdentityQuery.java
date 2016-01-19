package net.digitalid.core.resolution;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;

import net.digitalid.core.conversion.wrappers.value.EmptyWrapper;

import net.digitalid.core.entity.Entity;

import net.digitalid.core.exceptions.RequestErrorCode;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.Reply;

import net.digitalid.core.handler.core.CoreServiceExternalQuery;

import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.identifier.InternalNonHostIdentifier;

import net.digitalid.core.identity.SemanticType;

/**
 * Queries the identity of the given subject.
 * 
 * @see IdentityReply
 */
@Immutable
public final class IdentityQuery extends CoreServiceExternalQuery {
    
    /**
     * Stores the semantic type {@code query.identity@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("query.identity@core.digitalid.net").load(EmptyWrapper.XDF_TYPE);
    
    
    /**
     * Creates an identity query to retrieve the identity of the given subject.
     * 
     * @param subject the subject of this handler.
     */
    IdentityQuery(@Nonnull InternalNonHostIdentifier subject) {
        super(null, subject);
    }
    
    /**
     * Creates an identity query that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasEntity() : "This method has an entity.";
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    private IdentityQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, InternalException {
        super(entity, signature, recipient);
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return EmptyWrapper.encode(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Queries the identity.";
    }
    
    
    @Override
    @NonCommitting
    public @Nonnull IdentityReply executeOnHost() throws RequestException, SQLException {
        final @Nonnull InternalIdentifier subject = getSubject(); // The following exception should never be thrown as the condition is already checked in the packet class.
        if (!(subject instanceof InternalNonHostIdentifier)) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "The identity may only be queried of non-host identities."); }
        return new IdentityReply((InternalNonHostIdentifier) subject);
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply instanceof IdentityReply;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof IdentityQuery;
    }
    
    @Pure
    @Override
    public int hashCode() {
        return protectedHashCode();
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            return new IdentityQuery(entity, signature, recipient, block);
        }
        
    }
    
}
