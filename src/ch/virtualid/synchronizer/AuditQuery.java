package ch.virtualid.synchronizer;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.InternalQuery;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.service.Service;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Queries the audit of the given service for the given role.
 * 
 * @see AuditReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class AuditQuery extends InternalQuery {
    
    /**
     * Stores the semantic type {@code query.audit@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("query.audit@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the service whose audit is queried.
     */
    private final @Nonnull Service service;
    
    /**
     * Creates an internal query for the audit of the given service.
     * 
     * @param role the role to which this handler belongs.
     * @param service the service whose audit is queried.
     */
    AuditQuery(@Nonnull Role role, @Nonnull Service service) throws SQLException, PacketException, InvalidEncodingException {
        super(role, service.getRecipient(role));
        
        this.service = service;
    }
    
    /**
     * Creates an internal query that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    private AuditQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity.toNonHostEntity(), signature, recipient);
        
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
        return "Queries the audit of the " + service.getName() + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return service;
    }
    
    
    @Override
    public @Nonnull AuditReply executeOnHost() {
        return new AuditReply(getNonHostAccount(), service);
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply instanceof AuditReply && ((AuditReply) reply).getService().equals(service);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AuditQuery && this.service.equals(((AuditQuery) object).service);
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
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AuditQuery(entity, signature, recipient, block);
        }
        
    }
    
}
