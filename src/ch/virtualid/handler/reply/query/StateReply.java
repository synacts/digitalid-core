package ch.virtualid.handler.reply.query;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.handler.Reply;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Delivers the state of the given entity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class StateReply extends CoreServiceQueryReply {
    
    /**
     * Stores the semantic type {@code reply.module@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("reply.module@virtualid.ch").load(CoreService.FORMAT);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Stores the block containing the state of the given entity.
     * 
     * @invariant block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    private final @Nonnull Block block;
    
    /**
     * Creates a query reply to deliver the state of the given account.
     * 
     * @param account the account to which this query reply belongs.
     * 
     * @require block.getType().equals(CoreService.FORMAT) : "The block has the indicated type.";
     */
    public StateReply(@Nonnull Account account, @Nonnull Block block) {
        super(account);
        
        assert block.getType().equals(CoreService.FORMAT) : "The block has the indicated type.";
        
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
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     * @ensure !isOnHost() : "Query replies are never decoded on hosts.";
     */
    private StateReply(@Nullable Entity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws InvalidEncodingException {
        super(entity, signature, number);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
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
        return "Delivers the state.";
    }
    
    
    /**
     * The factory class for the surrounding method.
     */
    protected static final class Factory extends Reply.Factory {
        
        static { Reply.add(new Factory()); }
        
        @Pure
        @Override
        public @Nonnull SemanticType getType() {
            return TYPE;
        }
        
        @Pure
        @Override
        protected @Nonnull Reply create(@Nullable Entity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
            return new StateReply(entity, signature, number, block);
        }
        
    }
    
}
