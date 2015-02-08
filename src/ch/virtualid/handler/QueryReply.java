package ch.virtualid.handler;

import ch.virtualid.annotations.OnlyForHosts;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identity.IdentityReply;
import ch.virtualid.service.CoreServiceQueryReply;
import ch.xdf.HostSignatureWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a {@link Reply reply} to a {@link Query query}.
 * Query replies are read with getter methods on the handler.
 * 
 * @see CoreServiceQueryReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class QueryReply extends Reply {
    
    /**
     * Creates a query reply that encodes the content of a packet.
     * 
     * @param account the account to which this query reply belongs.
     */
    @OnlyForHosts
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
    protected QueryReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number) throws InvalidEncodingException {
        super(entity, signature, number);
        
        if (isOnHost()) throw new InvalidEncodingException("Query replies are never decoded on hosts.");
    }
    
}
