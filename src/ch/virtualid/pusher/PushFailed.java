package ch.virtualid.pusher;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.ActionReply;
import ch.virtualid.handler.ExternalAction;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.Identifier;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.Service;
import ch.virtualid.packet.Packet;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.Int64Wrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An action of this type is added to the audit if the {@link Pusher} failed to send an external action.
 * 
 * @see Pusher
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class PushFailed extends ExternalAction {
    
    /**
     * Stores the semantic type {@code number.failed.push@virtualid.ch}.
     */
    private static final @Nonnull SemanticType NUMBER = SemanticType.create("number.failed.push@virtualid.ch").load(Int64Wrapper.TYPE);
    
    /**
     * Stores the semantic type {@code subject.failed.push@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SUBJECT = SemanticType.create("subject.failed.push@virtualid.ch").load(IdentityClass.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code recipient.failed.push@virtualid.ch}.
     */
    private static final @Nonnull SemanticType RECIPIENT = SemanticType.create("recipient.failed.push@virtualid.ch").load(NonHostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code action.failed.push@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ACTION = SemanticType.create("action.failed.push@virtualid.ch").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code failed.push@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("failed.push@virtualid.ch").load(TupleWrapper.TYPE, NUMBER, SUBJECT, RECIPIENT, ACTION);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Stores the number of this failed push.
     */
    private final @Nonnull long number;
    
    // TODO: Maybe include a string that explains the problem?
    
    /**
     * Stores the action that could not be pushed.
     */
    private final @Nonnull ExternalAction action;
    
    /**
     * Creates an external action to indicate a failed push.
     * 
     * @param account the account to which this handler belongs.
     * @param action the action that could not be pushed.
     * 
     * @require account.getIdentity().equals(action.getEntityNotNull().getIdentity()) : "The account and the action's entity have the same identity.";
     */
    public PushFailed(@Nonnull Account account, @Nonnull ExternalAction action) {
        super(account, action.getSubject(), action.getRecipient());
        
        assert account.getIdentity().equals(action.getEntityNotNull().getIdentity()) : "The account and the action's entity have the same identity.";
        
        this.number = new SecureRandom().nextLong();
        this.action = action;
    }
    
    /**
     * Creates an external action that decodes the given block.
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
     */
    private PushFailed(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(4);
        this.number = new Int64Wrapper(elements.getNotNull(0)).getValue();
        
        final @Nonnull Identifier _subject = Identifier.create(elements.getNotNull(1));
        final @Nonnull HostIdentifier _recipient = new HostIdentifier(elements.getNotNull(2));
        final @Nonnull Block _block = new SelfcontainedWrapper(elements.getNotNull(3)).getElement();
        try {
            this.action = (ExternalAction) Method.get(entity, new SignatureWrapper(Packet.SIGNATURE, (Block) null, _subject), _recipient, _block);
        } catch (@Nonnull PacketException | ClassCastException exception) {
            throw new InvalidEncodingException("Could not decode the action of the failed push.", exception);
        }
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(4);
        elements.set(0, new Int64Wrapper(NUMBER, number).toBlock());
        elements.set(1, action.getSubject().toBlock().setType(SUBJECT));
        elements.set(2, action.getRecipient().toBlock().setType(RECIPIENT));
        elements.set(3, new SelfcontainedWrapper(ACTION, action.toBlock()).toBlock());
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Failed to push an external action of type " + action.getType().getAddress() + ".";
    }
    
    
    /**
     * Returns the number of this failed push.
     * 
     * @return the number of this failed push.
     */
    public long getNumber() {
        return number;
    }
    
    /**
     * Returns the action that could not be pushed.
     * 
     * @return the action that could not be pushed.
     */
    public @Nonnull ExternalAction getAction() {
        return action;
    }
    
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return action.getService();
    }
    
    
    @Pure
    @Override
    public @Nullable Class<ActionReply> getReplyClass() {
        return null;
    }
    
    @Override
    public @Nullable ActionReply executeOnHost() throws PacketException {
        throw new PacketException(PacketError.METHOD, "Failed push actions cannot be executed on a host.");
    }
    
    @Override
    public void executeOnClient() throws SQLException {
        assert isOnClient() : "This method is called on a client.";
        
        // TODO: Add this failed push to some list where the user can see it (see the Errors module).
        action.executeOnFailure();
    }
    
    @Override
    public void executeOnFailure() throws SQLException {
        throw new ShouldNeverHappenError("Failed push actions should never be pushed themselves.");
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
    public @Nullable Reply send() throws PacketException {
        throw new PacketException(PacketError.INTERNAL, "Failed push actions cannot be sent.");
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getAuditPermissions() {
        return action.getFailedAuditPermissions();
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return action.getFailedAuditRestrictions();
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new PushFailed(entity, signature, recipient, block);
        }
        
    }
    
}
