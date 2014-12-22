package ch.virtualid.handler.reply.query;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.handler.Reply;
import ch.virtualid.handler.query.internal.StateQuery;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replies the state of the given entity.
 * 
 * @see StateQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class StateReply extends CoreServiceQueryReply {
    
    /**
     * Stores the semantic type {@code reply.module@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("reply.module@virtualid.ch").load(CoreService.STATE);
    
    
    /**
     * Stores the block containing the state of the given entity.
     * 
     * @invariant block.getType().equals(TYPE) : "The block has the indicated type.";
     */
    private final @Nonnull Block block;
    
    /**
     * Creates a query reply for the state of the given account.
     * 
     * @param account the account to which this query reply belongs.
     * @param block the block that represents the state of the account.
     * 
     * @require block.getType().equals(CoreService.FORMAT) : "The block has the indicated type.";
     */
    public StateReply(@Nonnull Account account, @Nonnull Block block) {
        super(account);
        
        this.block = block.setType(TYPE);
    }
    
    /**
     * Creates a query reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * @param block the content which is to be decoded.
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure !isOnHost() : "Query replies are never decoded on hosts.";
     */
    private StateReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws InvalidEncodingException {
        super(entity, signature, number);
        
        this.block = block;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return block;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Replies the state.";
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Reply.Factory {
        
        static { Reply.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws InvalidEncodingException {
            return new StateReply(entity, signature, number, block);
        }
        
    }
    
}
