package net.digitalid.service.core.action.synchronizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.SelfcontainedWrapper;
import net.digitalid.service.core.block.wrappers.signature.HostSignatureWrapper;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.NonHostAccount;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.QueryReply;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.storage.Service;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.exceptions.external.ExternalException;

/**
 * Replies the state of the given entity.
 * 
 * @see StateQuery
 */
@Immutable
final class StateReply extends QueryReply {
    
    /**
     * Stores the semantic type {@code reply.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("reply.module@core.digitalid.net").load(SelfcontainedWrapper.XDF_TYPE);
    
    
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
    private StateReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, number);
        
        this.state = SelfcontainedWrapper.decodeNonNullable(block);
        this.service = Service.getModule(state.getType()).getService();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return SelfcontainedWrapper.encodeNonNullable(TYPE, state);
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
    void updateState() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        final @Nonnull StateModule module = Service.getModule(state.getType());
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
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            return new StateReply(entity, signature, number, block);
        }
        
    }
    
}
