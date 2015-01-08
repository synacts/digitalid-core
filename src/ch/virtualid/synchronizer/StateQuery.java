package ch.virtualid.synchronizer;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.handler.query.internal.CoreServiceInternalQuery;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.Service;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Queries the state of the given module for the given role.
 * 
 * @see StateReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class StateQuery extends CoreServiceInternalQuery {
    
    /**
     * Stores the semantic type {@code query.module@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("query.module@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the module whose state is queried.
     */
    private final @Nonnull BothModule module;
    
    /**
     * Creates an internal query for the state of the given module.
     * 
     * @param role the role to which this handler belongs.
     * @param module the module whose state is queried.
     */
    StateQuery(@Nonnull Role role, @Nonnull BothModule module) {
        super(role);
        
        this.module = module;
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
    private StateQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        this.module = Service.getModule(IdentityClass.create(block).toSemanticType());
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return module.getStateFormat().toBlock(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Queries the state.";
    }
    
    
    @Override
    protected @Nonnull StateReply executeOnHost(@Nonnull Agent agent) throws SQLException {
        return new StateReply(getNonHostAccount(), module.getState(getNonHostAccount(), agent));
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply instanceof StateReply && ((StateReply) reply).block.getType().equals(module.getStateFormat());
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof StateQuery && this.module.equals(((StateQuery) object).module);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + module.hashCode();
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
            return new StateQuery(entity, signature, recipient, block);
        }
        
    }
    
}
