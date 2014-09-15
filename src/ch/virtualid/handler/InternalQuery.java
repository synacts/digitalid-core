package ch.virtualid.handler;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.handler.query.internal.CoreServiceInternalQuery;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Internal queries can only be sent by {@link Client clients} and are always signed identity-based.
 * 
 * @invariant getEntity() != null : "The entity of this internal query is not null.";
 * @invariant getEntityNotNull().getIdentity().equals(getSubject().getIdentity()) : "The identity of the entity and the subject are the same.";
 * 
 * @see CoreServiceInternalQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
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
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    protected InternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
        super(entity, signature, recipient);
        
        if (!getEntityNotNull().getIdentity().equals(getSubject().getIdentity())) throw new InvalidEncodingException("The identity of the entity and the subject have to be the same for internal queries.");
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && other instanceof InternalQuery;
    }
    
    @Pure
    @Override
    public final boolean canBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public final boolean canOnlyBeSentByHosts() {
        return false;
    }
    
}
