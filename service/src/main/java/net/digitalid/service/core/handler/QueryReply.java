package net.digitalid.service.core.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.internal.InternalException;
import net.digitalid.utility.validation.state.Immutable;

import net.digitalid.service.core.block.wrappers.signature.HostSignatureWrapper;
import net.digitalid.service.core.entity.Account;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.handler.core.CoreServiceQueryReply;
import net.digitalid.service.core.identifier.InternalIdentifier;
import net.digitalid.service.core.identity.resolution.IdentityReply;
import net.digitalid.service.core.site.annotations.Hosts;

/**
 * This class models a {@link Reply reply} to a {@link Query query}.
 * Query replies are read with getter methods on the handler.
 * 
 * @see CoreServiceQueryReply
 */
@Immutable
public abstract class QueryReply extends Reply {
    
    /**
     * Creates a query reply that encodes the content of a packet.
     * 
     * @param account the account to which this query reply belongs.
     */
    @Hosts
    protected QueryReply(@Nonnull Account account) {
        super(account, account.getIdentity().getAddress());
    }
    
    /**
     * Creates a query reply that encodes the content of a packet.
     * This constructor is only needed for {@link IdentityReply}.
     * 
     * @param subject the subject of this handler.
     */
    protected QueryReply(@Nonnull InternalIdentifier subject) {
        super(null, subject);
    }
    
    /**
     * Creates a query reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure !isOnHost() : "Query replies are never decoded on hosts.";
     */
    protected QueryReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number) throws InternalException {
        super(entity, signature, number);
        
        if (isOnHost()) { throw InternalException.get("Query replies are never decoded on hosts."); }
    }
    
}
