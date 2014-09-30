package ch.virtualid.pusher;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Aspect;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.exceptions.ShouldNeverHappenError;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.handler.ExternalAction;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.Packet;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.virtualid.server.Host;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.CompressionWrapper;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.FailedEncodingException;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An action of this type is added to the audit if an {@link ExternalAction external action} on a {@link Host host} has a {@link ActionReply reply}.
 * 
 * @see Pusher
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class PushReturned extends ExternalAction {
    
    /**
     * Stores the aspect of the reply being invalid in the observed action.
     */
    public static final @Nonnull Aspect INVALID = new Aspect(PushReturned.class, "invalid reply");
    
    
    /**
     * Stores the semantic type {@code valid.returned.push@virtualid.ch}.
     */
    private static final @Nonnull SemanticType VALID = SemanticType.create("valid.returned.push@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code reply.returned.push@virtualid.ch}.
     */
    private static final @Nonnull SemanticType REPLY = SemanticType.create("reply.returned.push@virtualid.ch").load(Packet.SIGNATURE);
    
    /**
     * Stores the semantic type {@code returned.push@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("returned.push@virtualid.ch").load(TupleWrapper.TYPE, VALID, REPLY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Stores whether the reply is valid.
     */
    private final boolean valid;
    
    /**
     * Stores the reply which was returned.
     * 
     * @invariant reply.getSignature() instanceof HostSignatureWrapper : "The reply is signed by a host.";
     */
    private final @Nonnull ActionReply reply;
    
    /**
     * Creates an external action to indicate a failed push.
     * 
     * @param account the account to which this handler belongs.
     * @param valid whether the given reply is valid.
     * @param reply the reply that was returned by an action.
     * 
     * @require reply.getSignature() instanceof HostSignatureWrapper : "The reply is signed by a host (and the signature thus not null).";
     * @require account.getIdentity().equals(reply.getEntityNotNull().getIdentity()) : "The account and the reply's entity have the same identity.";
     */
    public PushReturned(@Nonnull Account account, boolean valid, @Nonnull ActionReply reply) {
        super(account, account.getIdentity().getAddress(), account.getHost().getIdentifier());
        
        assert reply.getSignature() instanceof HostSignatureWrapper : "The reply is signed by a host (and the signature thus not null).";
        assert account.getIdentity().equals(reply.getEntityNotNull().getIdentity()) : "The account and the reply's entity have the same identity.";
        
        this.valid = valid;
        this.reply = reply;
    }
    
    /**
     * Creates an external action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    private PushReturned(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, FailedIdentityException, SQLException, InvalidDeclarationException {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        this.valid = new BooleanWrapper(elements.getNotNull(0)).getValue();
        
        final @Nonnull SignatureWrapper _signature = SignatureWrapper.decodeUnverified(elements.getNotNull(1));
        if (!(_signature instanceof HostSignatureWrapper)) throw new InvalidEncodingException("Replies have to be signed by a host.");
        final @Nonnull CompressionWrapper _compression = new CompressionWrapper(_signature.getElementNotNull());
        final @Nonnull SelfcontainedWrapper _content = new SelfcontainedWrapper(_compression.getElementNotNull());
        try {
            this.reply = (ActionReply) Reply.get(entity, (HostSignatureWrapper) _signature, _content.getElement());
        } catch (@Nonnull PacketException | ClassCastException exception) {
            throw new InvalidEncodingException("Could not decode the reply to an external action.", exception);
        }
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(2);
        elements.set(0, new BooleanWrapper(VALID, valid).toBlock());
        elements.set(1, reply.getSignatureNotNull().toBlock().setType(TYPE));
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "An external action returned a reply of type " + reply.getType().getAddress() + ".";
    }
    
    
    /**
     * Returns whether the reply is valid.
     * 
     * @return whether the reply is valid.
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Returns the reply which was returned.
     * 
     * @return the reply which was returned.
     */
    public @Nonnull ActionReply getReply() {
        return reply;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getService() {
        return reply.getService();
    }
    
    
    @Override
    public @Nullable ActionReply executeOnHost() throws PacketException {
        throw new PacketException(PacketError.REQUEST);
    }
    
    @Override
    public void executeOnClient() throws SQLException {
        assert isOnClient() : "This method is called on a client.";
        
        if (!isValid()) notify(INVALID); // TODO: Also add it to the Errors module?
        else reply.executeBySynchronizer();
    }
    
    @Override
    public void executeOnFailure() {
        throw new ShouldNeverHappenError("Returned push replies should never be pushed themselves.");
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return AgentPermissions.NONE;
    }
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return false;
    }
    
    @Override
    public @Nullable Reply send() throws FailedEncodingException {
        throw new FailedEncodingException("Returned push replies cannot be sent.");
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getAuditPermissions() {
        return reply.getAuditPermissions();
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return reply.getAuditRestrictions();
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
            return new PushReturned(entity, signature, recipient, block);
        }
        
    }
    
}
