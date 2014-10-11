package ch.virtualid.handler.query.internal;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.InvalidDeclarationException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.reply.query.StateReply;
import ch.virtualid.exceptions.external.IdentityNotFoundException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.xdf.Block;
import ch.xdf.EmptyWrapper;
import ch.xdf.SignatureWrapper;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Retrieves the state of the given role.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class StateQuery extends CoreServiceInternalQuery {
    
    /**
     * Stores the semantic type {@code query.module@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("query.module@virtualid.ch").load(EmptyWrapper.TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Creates an internal query to retrieve the state of the given role.
     * 
     * @param role the role to which this handler belongs.
     */
    public StateQuery(@Nonnull Role role) {
        super(role);
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
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    private StateQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, SQLException, IdentityNotFoundException, InvalidDeclarationException {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new EmptyWrapper(TYPE).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Retrieves the state.";
    }
    
    
    @Override
    protected @Nonnull StateReply executeOnHost(@Nonnull Agent agent) throws SQLException {
        final @Nonnull Account account = (Account) getEntityNotNull();
        return new StateReply(account, CoreService.SERVICE.getAll(account, agent));
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, SQLException, IdentityNotFoundException, InvalidDeclarationException {
            return new StateQuery(entity, signature, recipient, block);
        }
        
    }
    
}
