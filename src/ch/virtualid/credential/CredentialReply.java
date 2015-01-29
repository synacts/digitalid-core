package ch.virtualid.credential;

import ch.virtualid.agent.RandomizedAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.entity.NonHostAccount;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.service.CoreServiceQueryReply;
import ch.virtualid.service.Service;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replies the parameters of a new credential.
 * 
 * @see CredentialInternalQuery
 * @see CredentialExternalQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
final class CredentialReply extends CoreServiceQueryReply {
    
    /**
     * Stores the semantic type {@code reply.internal.credential@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.create("reply.internal.credential@virtualid.ch").load(TupleWrapper.TYPE, RandomizedAgentPermissions.TYPE, SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the restrictions for which the credential is issued.
     */
    private final @Nullable Restrictions restrictions;
    
    /**
     * Stores the certifying base of the issued credential.
     */
    private final @Nonnull Element c;
    
    /**
     * Stores the certifying exponent of the issued credential.
     */
    private final @Nonnull Exponent e;
    
    /**
     * Stores the serial number of the issued credential.
     */
    private final @Nonnull Exponent i;
    
    /**
     * Creates a query reply for the parameters of a new credential.
     * 
     * @param account the account to which this query reply belongs.
     * @param restrictions the restrictions for which the credential is issued.
     * @param c the certifying base of the issued credential.
     * @param e the certifying exponent of the issued credential.
     * @param i the serial number of the issued credential.
     */
    CredentialReply(@Nonnull NonHostAccount account, @Nullable Restrictions restrictions, @Nonnull Element c, @Nonnull Exponent e, @Nonnull Exponent i) {
        super(account);
        
        this.restrictions = restrictions;
        this.c = c;
        this.e = e;
        this.i = i;
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
    private CredentialReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
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
    public @Nonnull String toString() {
        return "Replies the parameters of a new credential.";
    }
    
    
    /**
     * Updates the state of the given entity without committing.
     * 
     * @require isOnClient() : "This method is called on a client.";
     */
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
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new CredentialReply(entity, signature, number, block);
        }
        
    }
    
}
