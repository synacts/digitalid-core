package net.digitalid.service.core.action.synchronizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.HostSignatureWrapper;
import net.digitalid.service.core.storage.Service;
import net.digitalid.service.core.entity.NonHostAccount;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.QueryReply;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.identity.IdentityImplementation;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Replies the audit of the given entity.
 * 
 * @see AuditQuery
 */
@Immutable
final class AuditReply extends QueryReply {
    
    /**
     * Stores the semantic type {@code reply.audit@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("reply.audit@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the service to which this audit reply belongs.
     */
    private final @Nonnull Service service;
    
    /**
     * Creates a query reply for the audit of the given account.
     * 
     * @param account the account to which this query reply belongs.
     * @param service the service to which this audit reply belongs.
     */
    AuditReply(@Nonnull NonHostAccount account, @Nonnull Service service) {
        super(account);
        
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
    private AuditReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, number);
        
        this.service = Service.getService(IdentityImplementation.create(block).castTo(SemanticType.class));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return service.getType().toBlock(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replies the audit.";
    }
    
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return service;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AuditReply && this.service.equals(((AuditReply) object).service);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + service.hashCode();
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
            return new AuditReply(entity, signature, number, block);
        }
        
    }
    
}
