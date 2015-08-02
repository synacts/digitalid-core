package net.digitalid.core.pusher;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostAccount;
import net.digitalid.core.errors.ShouldNeverHappenError;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.ActionReply;
import net.digitalid.core.handler.ExternalAction;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.packet.Packet;
import net.digitalid.core.packet.Response;
import net.digitalid.core.service.Service;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.EncryptionWrapper;
import net.digitalid.core.wrappers.Int64Wrapper;
import net.digitalid.core.wrappers.SelfcontainedWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * An action of this type is added to the audit if the {@link Pusher} failed to send an external action.
 * 
 * @see Pusher
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class PushFailed extends ExternalAction {
    
    /**
     * Stores the semantic type {@code number.failed.push@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NUMBER = SemanticType.map("number.failed.push@core.digitalid.net").load(Int64Wrapper.TYPE);
    
    /**
     * Stores the semantic type {@code subject.failed.push@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SUBJECT = SemanticType.map("subject.failed.push@core.digitalid.net").load(SignatureWrapper.SUBJECT);
    
    /**
     * Stores the semantic type {@code recipient.failed.push@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType RECIPIENT = SemanticType.map("recipient.failed.push@core.digitalid.net").load(EncryptionWrapper.RECIPIENT);
    
    /**
     * Stores the semantic type {@code action.failed.push@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ACTION = SemanticType.map("action.failed.push@core.digitalid.net").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code failed.push@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("failed.push@core.digitalid.net").load(TupleWrapper.TYPE, NUMBER, SUBJECT, RECIPIENT, ACTION);
    
    
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
    PushFailed(@Nonnull NonHostAccount account, @Nonnull ExternalAction action) {
        super(account, action.getSubject(), action.getRecipient());
        
        assert account.getIdentity().equals(action.getEntityNotNull().getIdentity()) : "The account and the action's entity have the same identity.";
        
        this.number = new Random().nextLong();
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
    @NonCommitting
    private PushFailed(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(4);
        this.number = new Int64Wrapper(elements.getNonNullable(0)).getValue();
        
        final @Nonnull InternalIdentifier _subject = IdentifierClass.create(elements.getNonNullable(1)).toInternalIdentifier();
        final @Nonnull HostIdentifier _recipient = IdentifierClass.create(elements.getNonNullable(2)).toHostIdentifier();
        final @Nonnull Block _block = new SelfcontainedWrapper(elements.getNonNullable(3)).getElement();
        try {
            this.action = (ExternalAction) Method.get(entity, new SignatureWrapper(Packet.SIGNATURE, (Block) null, _subject), _recipient, _block);
        } catch (@Nonnull PacketException | ClassCastException exception) {
            throw new InvalidEncodingException("Could not decode the action of the failed push.", exception);
        }
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<>(4);
        elements.set(0, new Int64Wrapper(NUMBER, number).toBlock());
        elements.set(1, action.getSubject().toBlock().setType(SUBJECT));
        elements.set(2, action.getRecipient().toBlock().setType(RECIPIENT));
        elements.set(3, new SelfcontainedWrapper(ACTION, action.toBlock()).toBlock());
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Failed to push an external action of type " + action.getType().getAddress() + ".";
    }
    
    
    /**
     * Returns the number of this failed push.
     * 
     * @return the number of this failed push.
     */
    @Pure
    public long getNumber() {
        return number;
    }
    
    /**
     * Returns the action that could not be pushed.
     * 
     * @return the action that could not be pushed.
     */
    @Pure
    public @Nonnull ExternalAction getAction() {
        return action;
    }
    
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return action.getService();
    }
    
    
    @Override
    public @Nullable ActionReply executeOnHost() throws PacketException {
        throw new PacketException(PacketError.METHOD, "Failed push actions cannot be executed on a host.");
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply == null;
    }
    
    @Override
    @NonCommitting
    public void executeOnClient() throws SQLException {
        // TODO: Add this failed push to some list where the user can see it (see the Errors module).
        action.executeOnFailure();
    }
    
    @Override
    @NonCommitting
    public void executeOnFailure() throws SQLException {
        throw new ShouldNeverHappenError("Failed push actions should never be pushed themselves.");
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return false;
    }
    
    @Override
    public @Nullable Response send() throws PacketException {
        throw new PacketException(PacketError.INTERNAL, "Failed push actions cannot be sent.");
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getAuditPermissions() {
        return action.getFailedAuditPermissions();
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return action.getFailedAuditRestrictions();
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof PushFailed) {
            final @Nonnull PushFailed other = (PushFailed) object;
            return this.number == other.number && this.action.equals(other.action);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + (int) (number ^ (number >>> 32));
        hash = 89 * hash + action.hashCode();
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull BothModule getModule() {
        return PusherModule.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new PushFailed(entity, signature, recipient, block);
        }
        
    }
    
}
