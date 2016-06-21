package net.digitalid.core.handler;

import javax.annotation.Nonnull;

import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.client.Client;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestErrorCode;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.handler.core.CoreServiceInternalQuery;
import net.digitalid.core.identification.identifier.HostIdentifier;

/**
 * Internal queries can only be sent by {@link Client clients} and are always signed identity-based.
 * 
 * @invariant hasEntity() : "This internal query has an entity.";
 * @invariant isNonHost() : "This internal query belongs to a non-host.";
 * @invariant getEntityNotNull().getIdentity().equals(getSubject().getIdentity()) : "The identity of the entity and the subject are the same.";
 * 
 * @see CoreServiceInternalQuery
 */
@Immutable
public abstract class InternalQuery extends Query implements InternalMethod {
    
    /**
     * Creates an internal query that encodes the content of a packet for the given recipient.
     * 
     * @param role the role to which this handler belongs.
     * @param recipient the recipient of this method.
     */
    protected InternalQuery(@Nonnull Role role, @Nonnull HostIdentifier recipient) {
        super(role, role.getIdentity().getAddress(), recipient);
    }
    
    /**
     * Creates an internal query that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    @NonCommitting
    protected InternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws ExternalException {
        super(entity, signature, recipient);
        
        if (!isNonHost()) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "Internal queries have to belong to a non-host."); }
        if (!getEntityNotNull().getIdentity().equals(getSubject().getIdentity())) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "The identity of the entity and the subject have to be the same for internal queries."); }
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && other instanceof InternalQuery;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return Restrictions.MIN;
    }
    
}
