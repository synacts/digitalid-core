package ch.virtualid.handler;

import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.handler.reply.query.CoreServiceQueryReply;
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
 * @version 2.0
 */
public abstract class QueryReply extends Reply {
    
    /**
     * Creates a query reply that encodes the content of a packet.
     * 
     * @param account the account to which this query reply belongs.
     */
    protected QueryReply(@Nonnull Account account) {
        super(account);
    }
    
    /**
     * Creates a query reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    protected QueryReply(@Nullable Entity entity, @Nonnull HostSignatureWrapper signature, long number) {
        super(entity, signature, number);
    }
    
}
