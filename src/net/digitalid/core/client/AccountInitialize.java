package net.digitalid.core.client;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.cryptography.PublicKey;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NativeRole;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidDeclarationException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.Method;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identifier.NonHostIdentifier;
import net.digitalid.core.identity.Category;
import net.digitalid.core.identity.FreezablePredecessors;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.identity.NonHostIdentity;
import net.digitalid.core.identity.Predecessor;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.Successor;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.password.PasswordModule;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.CoreServiceInternalAction;
import net.digitalid.core.tuples.FreezablePair;
import net.digitalid.core.tuples.ReadOnlyPair;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * Initializes a new account with the given states.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class AccountInitialize extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code state.initialize.account@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.create("state.initialize.account@core.digitalid.net").load(TupleWrapper.TYPE, Predecessor.TYPE, CoreService.STATE);
    
    /**
     * Stores the semantic type {@code initialize.account@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("initialize.account@core.digitalid.net").load(ListWrapper.TYPE, STATE);
    
    
    /**
     * Stores the states to merge into the new account.
     */
    private final @Nonnull @Frozen @NonNullableElements ReadOnlyList<ReadOnlyPair<Predecessor, Block>> states;
    
    /**
     * Creates an action to initialize a new account.
     * 
     * @param role the role to which this handler belongs.
     * @param states the states to merge into the new account.
     */
    @NonCommitting
    AccountInitialize(@Nonnull NativeRole role, @Nonnull @Frozen @NonNullableElements ReadOnlyList<ReadOnlyPair<Predecessor, Block>> states) throws SQLException, IOException, PacketException, ExternalException {
        super(role);
        
        this.states = states;
    }
    
    /**
     * Creates an action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    @NonCommitting
    private AccountInitialize(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        final @Nonnull InternalNonHostIdentifier subject = getSubject().toInternalNonHostIdentifier();
        if (isOnHost() && FreezablePredecessors.exist(subject)) throw new PacketException(PacketError.METHOD, "The subject " + subject + " is already initialized.");
        
        final @Nonnull Category category = entity.getIdentity().getCategory();
        final @Nonnull ReadOnlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        if (elements.size() > 1 && !category.isInternalPerson()) throw new InvalidDeclarationException("Only internal persons may have more than one predecessor.", subject, null);
        
        final @Nonnull FreezableList<ReadOnlyPair<Predecessor, Block>> states = new FreezableArrayList<>(elements.size());
        for (final @Nonnull Block element : elements) {
            final @Nonnull TupleWrapper tuple = new TupleWrapper(element);
            final @Nonnull Predecessor predecessor = new Predecessor(tuple.getElementNotNull(0));
            final @Nonnull NonHostIdentifier identifier = predecessor.getIdentifier();
            final @Nonnull NonHostIdentity identity = identifier.getIdentity();
            final @Nonnull String message = "The claimed predecessor " + identifier + " of " + subject;
            if (!(identity.getCategory().isExternalPerson() && category.isInternalPerson() || identity.getCategory() == category)) throw new InvalidDeclarationException(message + " has a wrong category.", subject, null);
            if (identifier instanceof InternalNonHostIdentifier) {
                if (!FreezablePredecessors.get((InternalNonHostIdentifier) identifier).equals(predecessor.getPredecessors())) throw new InvalidDeclarationException(message + " has other predecessors.", subject, null);
            } else {
                if (predecessor.getPredecessors().isNotEmpty()) throw new InvalidDeclarationException(message + " is an external person and may not have any predecessors.", subject, null);
            }
            if (!Successor.getReloaded(identifier).equals(subject)) throw new InvalidDeclarationException(message + " does not link back.", subject, null);
            states.add(new FreezablePair<>(predecessor, tuple.getElement(1)).freeze());
        }
        this.states = states.freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<>(states.size());
        for (final @Nonnull ReadOnlyPair<Predecessor, Block> state : states) {
            elements.add(new TupleWrapper(STATE, state.getElement0().toBlock(), state.getElement1()).toBlock());
        }
        return new ListWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Initializes the new account.";
    }
    
    
    @Pure
    @Override
    public @Nullable PublicKey getPublicKey() {
        return null;
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getAuditPermissions() {
        return FreezableAgentPermissions.GENERAL_WRITE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return Restrictions.MAX;
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws SQLException {
        final @Nonnull NonHostEntity entity = getNonHostEntity();
        
        try {
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
            final @Nonnull FreezablePredecessors predecessors = new FreezablePredecessors(states.size());
            final @Nonnull FreezableList<NonHostIdentity> identities = new FreezableArrayList<>(states.size());
            for (final @Nonnull ReadOnlyPair<Predecessor, Block> state : states) {
                final @Nonnull Predecessor predecessor = state.getElement0();
                identities.add(predecessor.getIdentifier().getIdentity().toNonHostIdentity());
                CoreService.SERVICE.addState(entity, state.getElement1());
                predecessors.add(predecessor);
            }
            Mapper.mergeIdentities(identities.freeze(), entity.getIdentity());
            predecessors.set(getSubject().toInternalNonHostIdentifier(), null);
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("A problem occurred while adding a state.", exception);
        }
        
        if (states.isEmpty()) PasswordModule.set(entity, "");
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return false;
    }
    
    @Pure
    @Override
    public @Nonnull AccountClose getReverse() {
        return new AccountClose(getRole().toNativeRole(), null);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AccountInitialize && this.states.equals(((AccountInitialize) object).states);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + states.hashCode();
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull BothModule getModule() {
        return CoreService.SERVICE;
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
            return new AccountInitialize(entity, signature, recipient, block);
        }
        
    }
    
}
