package net.digitalid.service.core.handler;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.core.CoreServiceInternalQuery;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

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
    protected InternalQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity, signature, recipient);
        
        if (!isNonHost()) throw new PacketException(PacketErrorCode.IDENTIFIER, "Internal queries have to belong to a non-host.");
        if (!getEntityNotNull().getIdentity().equals(getSubject().getIdentity())) throw new PacketException(PacketErrorCode.IDENTIFIER, "The identity of the entity and the subject have to be the same for internal queries.");
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
