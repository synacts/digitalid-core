package ch.virtualid.synchronizer;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.credential.Credential;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NonHostAccount;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.InternalQuery;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.service.CoreService;
import ch.virtualid.service.Service;
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
 * @version 1.0
 */
final class StateQuery extends InternalQuery {
    
    /**
     * Stores the semantic type {@code query.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("query.module@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    
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
    @DoesNotCommit
    StateQuery(@Nonnull Role role, @Nonnull BothModule module) throws SQLException, PacketException, InvalidEncodingException {
        super(role, module.getService().getRecipient(role));
        
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
    @DoesNotCommit
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
    public @Nonnull String getDescription() {
        return "Queries the state of the " + module.getClass().getSimpleName() + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return module.getService();
    }
    
    
    @Override
    @DoesNotCommit
    public @Nonnull StateReply executeOnHost() throws PacketException, SQLException {
        final @Nonnull Service service = module.getService();
        final @Nonnull NonHostAccount account = getNonHostAccount();
        if (module.getService().equals(CoreService.SERVICE)) {
            final @Nonnull Agent agent = getSignatureNotNull().getAgentCheckedAndRestricted(account, null);
            return new StateReply(account, module.getState(account, agent.getPermissions(), agent.getRestrictions(), agent), service);
        } else {
            final @Nonnull Credential credential = getSignatureNotNull().toCredentialsSignatureWrapper().getCredentials().getNotNull(0);
            final @Nullable ReadonlyAgentPermissions permissions = credential.getPermissions();
            final @Nullable Restrictions restrictions = credential.getRestrictions();
            if (permissions == null || restrictions == null) throw new PacketException(PacketError.AUTHORIZATION, "For state queries, neither the permissions nor the restrictions may be null.");
            return new StateReply(account, module.getState(account, permissions, restrictions, null), service);
        }
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply instanceof StateReply && ((StateReply) reply).state.getType().equals(module.getStateFormat());
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
        @DoesNotCommit
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new StateQuery(entity, signature, recipient, block);
        }
        
    }
    
}
