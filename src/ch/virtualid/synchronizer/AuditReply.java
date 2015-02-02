package ch.virtualid.synchronizer;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.NonHostAccount;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.QueryReply;
import ch.virtualid.handler.Reply;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.service.Service;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replies the audit of the given entity.
 * 
 * @see AuditQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class AuditReply extends QueryReply {
    
    /**
     * Stores the semantic type {@code reply.audit@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("reply.audit@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    
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
    private AuditReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, number);
        
        this.service = Service.getService(IdentityClass.create(block).toSemanticType());
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
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AuditReply(entity, signature, number, block);
        }
        
    }
    
}
