package ch.virtualid.handler.query.external;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.reply.query.IdentityReply;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Retrieves the identity of the given subject.
 * 
 * @see IdentityReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class IdentityQuery extends CoreServiceExternalQuery {
    
    /**
     * Stores the semantic type {@code query.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("query.identity@virtualid.ch").load(BooleanWrapper.TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Creates an external query to retrieve the identity of the given subject.
     * 
     * @param subject the subject of this handler.
     */
    public IdentityQuery(@Nonnull Identifier subject) {
        super(null, subject);
    }
    
    /**
     * Creates an external query that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    private IdentityQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new BooleanWrapper(TYPE, true).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Retrieves the identity.";
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return AgentPermissions.NONE;
    }
    
    
    @Override
    public @Nonnull IdentityReply executeOnHost() throws PacketException, SQLException {
        assert isOnHost() : "This method is called on a host.";
        assert getSignature() != null : "The signature of this handler is not null.";
        
        return new IdentityReply((Account) getEntityNotNull());
    }
    
    
    /**
     * The factory class for the surrounding method.
     */
    protected static final class Factory extends Method.Factory {
        
        static { Method.add(new Factory()); }
        
        @Pure
        @Override
        public @Nonnull SemanticType getType() {
            return TYPE;
        }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
            return new IdentityQuery(entity, signature, recipient, block);
        }
        
    }
    
}
