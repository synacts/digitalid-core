package ch.virtualid.handler.query.internal;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.reply.query.StateReply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.xdf.Block;
import ch.xdf.EmptyWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Queries the state of the given role.
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
    public static final @Nonnull SemanticType TYPE = SemanticType.create("query.module@virtualid.ch").load(EmptyWrapper.TYPE);
    
    
    /**
     * Creates an internal query for the state of the given role.
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
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure isOnHost() : "Queries are only decoded on hosts.";
     */
    private StateQuery(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new EmptyWrapper(TYPE).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Queries the state.";
    }
    
    
    @Pure
    @Override
    public @Nonnull Class<StateReply> getReplyClass() {
        return StateReply.class;
    }
    
    @Override
    protected @Nonnull StateReply executeOnHost(@Nonnull Agent agent) throws SQLException {
        return new StateReply(getNonHostAccount(), CoreService.SERVICE.getState(getNonHostAccount(), agent));
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
