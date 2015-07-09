package net.digitalid.core.synchronizer;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.entity.NonHostAccount;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.QueryReply;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.service.Service;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.HostSignatureWrapper;
import net.digitalid.core.wrappers.SelfcontainedWrapper;

/**
 * Replies the state of the given entity.
 * 
 * @see StateQuery
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
final class StateReply extends QueryReply {
    
    /**
     * Stores the semantic type {@code reply.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("reply.module@core.digitalid.net").load(SelfcontainedWrapper.TYPE);
    
    
    /**
     * Stores the state of the given entity.
     */
    final @Nonnull Block state;
    
    /**
     * Stores the service to which this state reply belongs.
     */
    private final @Nonnull Service service;
    
    /**
     * Creates a query reply for the state of the given account.
     * 
     * @param account the account to which this query reply belongs.
     * @param block the block that contains the state of the account.
     * @param service the service to which this state reply belongs.
     */
    StateReply(@Nonnull NonHostAccount account, @Nonnull Block block, @Nonnull Service service) {
        super(account);
        
        this.state = block;
        this.service = service;
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
    @NonCommitting
    private StateReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, number);
        
        this.state = new SelfcontainedWrapper(block).getElement();
        this.service = Service.getModule(state.getType()).getService();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new SelfcontainedWrapper(TYPE, state).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replies the state.";
    }
    
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return service;
    }
    
    
    /**
     * Updates the state of the given entity without committing.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
    @NonCommitting
    void updateState() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull BothModule module = Service.getModule(state.getType());
        final @Nonnull Role role = getRole();
        module.removeState(role);
        module.addState(role, state);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof StateReply && this.state.equals(((StateReply) object).state);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + state.hashCode();
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
        @NonCommitting
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new StateReply(entity, signature, number, block);
        }
        
    }
    
}
