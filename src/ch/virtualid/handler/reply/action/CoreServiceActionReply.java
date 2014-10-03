package ch.virtualid.handler.reply.action;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.xdf.HostSignatureWrapper;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the {@link ActionReply action replies} of the {@link CoreService core service}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class CoreServiceActionReply extends ActionReply {
    
    /**
     * Creates an action reply that encodes the content of a packet.
     * 
     * @param account the account to which this action reply belongs.
     */
    protected CoreServiceActionReply(@Nonnull Account account) {
        super(account);
    }
    
    /**
     * Creates an action reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    protected CoreServiceActionReply(@Nullable Entity entity, @Nonnull HostSignatureWrapper signature, long number) throws InvalidEncodingException {
        super(entity, signature, number);
    }
    
    
    @Pure
    @Override
    public final @Nonnull SemanticType getService() {
        return CoreService.TYPE;
    }
    
}
