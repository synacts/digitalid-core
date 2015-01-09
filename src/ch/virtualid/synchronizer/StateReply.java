package ch.virtualid.synchronizer;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.service.CoreServiceQueryReply;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.service.Service;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import java.io.IOException;
import java.sql.SQLException;
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
    public static final @Nonnull SemanticType TYPE = SemanticType.create("reply.module@virtualid.ch").load(SelfcontainedWrapper.TYPE);
    
    
    /**
     * Stores the block that contains the state of the given entity.
     */
    final @Nonnull Block block;
    
    /**
     * Creates a query reply for the state of the given account.
     * 
     * @param account the account to which this query reply belongs.
     * @param block the block that contains the state of the account.
     */
    StateReply(@Nonnull Account account, @Nonnull Block block) {
        super(account);
        
        this.block = block;
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
    private StateReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, number);
        
        this.block = new SelfcontainedWrapper(block).getElement();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new SelfcontainedWrapper(TYPE, block).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Replies the state.";
    }
    
    
    /**
     * Updates the state of the given entity without committing.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
    void updateState() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull BothModule module = Service.getModule(block.getType());
        final @Nonnull Role role = getRole();
        module.removeState(role);
        module.addState(role, block);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof StateReply && this.block.equals(((StateReply) object).block);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + block.hashCode();
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
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new StateReply(entity, signature, number, block);
        }
        
    }
    
}
