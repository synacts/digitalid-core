package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidDeclarationException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.Predecessor;
import ch.virtualid.identity.Predecessors;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.Successor;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.CoreService;
import ch.virtualid.module.both.Passwords;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * Initializes a new account with the given states.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.9
 */
public final class AccountInitialize extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code state.initialize.account@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.create("state.initialize.account@virtualid.ch").load(TupleWrapper.TYPE, Predecessor.TYPE, CoreService.STATE);
    
    /**
     * Stores the semantic type {@code initialize.account@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("initialize.account@virtualid.ch").load(ListWrapper.TYPE, STATE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Stores the states to merge into the new account.
     * 
     * @invariant states.isFrozen() : "The list of states is frozen.";
     * @invariant states.doesNotContainNull() : "The list of states does not contain null.";
     */
    private final @Nonnull ReadonlyList<Pair<Predecessor, Block>> states;
    
    /**
     * Creates an action to initialize a new account.
     * 
     * @param role the role to which this handler belongs.
     * @param states the states to merge into the new account.
     * 
     * @require role.isNative() : "The role is native.";
     * @require states.isFrozen() : "The list of states is frozen.";
     * @require states.doesNotContainNull() : "The list of states does not contain null.";
     */
    public AccountInitialize(@Nonnull Role role, @Nonnull ReadonlyList<Pair<Predecessor, Block>> states) throws SQLException, IOException, PacketException, ExternalException {
        super(role);
        
        assert role.isNative() : "The role is native.";
        assert states.isFrozen() : "The list of states is frozen.";
        assert states.doesNotContainNull() : "The list of states does not contain null.";
        
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
    private AccountInitialize(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull InternalNonHostIdentifier subject = (InternalNonHostIdentifier) getSubject();
        if (Predecessors.exist(subject)) throw new PacketException(PacketError.METHOD, "The subject " + subject + " is already initialized.");
        
        final @Nonnull Category category = entity.getIdentity().getCategory();
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        if (elements.size() > 1 && !category.isInternalPerson()) throw new InvalidDeclarationException("Only internal persons may have more than one predecessor.", subject, null);
        
        final @Nonnull FreezableList<Pair<Predecessor, Block>> states = new FreezableArrayList<Pair<Predecessor, Block>>(elements.size());
        for (final @Nonnull Block element : elements) {
            final @Nonnull TupleWrapper tuple = new TupleWrapper(element);
            final @Nonnull Predecessor predecessor = new Predecessor(tuple.getElementNotNull(0));
            final @Nonnull NonHostIdentifier identifier = predecessor.getIdentifier();
            final @Nonnull NonHostIdentity identity = identifier.getIdentity();
            final @Nonnull String message = "The claimed predecessor " + identifier + " of " + subject;
            if (!(identity.getCategory().isExternalPerson() && category.isInternalPerson() || identity.getCategory() == category)) throw new InvalidDeclarationException(message + " has a wrong category.", subject, null);
            if (identifier instanceof InternalNonHostIdentifier) {
                if (!Predecessors.get((InternalNonHostIdentifier) identifier).equals(predecessor.getPredecessors())) throw new InvalidDeclarationException(message + " has other predecessors.", subject, null);
            } else {
                if (predecessor.getPredecessors().isNotEmpty()) throw new InvalidDeclarationException(message + " is an external person and may not have any predecessors.", subject, null);
            }
            if (!Successor.getReloaded(identifier, false).equals(subject)) throw new InvalidDeclarationException(message + " does not link back.", subject, null);
            states.add(new Pair<Predecessor, Block>(predecessor, tuple.getElement(1)));
        }
        this.states = states.freeze();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<Block>(states.size());
        for (final @Nonnull Pair<Predecessor, Block> state : states) {
            elements.add(new TupleWrapper(STATE, new FreezableArray<Block>(state.getValue0().toBlock(), state.getValue1()).freeze()).toBlock());
        }
        return new ListWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Initializes the new account.";
    }
    
    
    @Pure
    @Override
    public @Nullable PublicKey getPublicKey() {
        return null;
    }
    
    
    @Override
    protected void executeOnBoth() throws SQLException {
        final @Nonnull Entity entity = getEntityNotNull();
        
        try {
            final @Nonnull FreezableList<NonHostIdentity> identities = new FreezableArrayList<NonHostIdentity>(states.size());
            for (final @Nonnull Pair<Predecessor, Block> state : states) {
                identities.add(state.getValue0().getIdentifier().getIdentity().toNonHostIdentity());
                CoreService.SERVICE.addState(entity, state.getValue1());
            }
            Mapper.mergeIdentities(identities.freeze(), entity.getIdentity().toInternalNonHostIdentity());
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("A problem occurred while adding a state.", exception);
        }
        
        if (states.isEmpty()) {
            Passwords.set(entity, "");
            // TODO: Create the root context.
        }
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getAuditPermissions() {
        return AgentPermissions.GENERAL_WRITE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return Restrictions.MAX;
    }
    
    
    @Pure
    @Override
    public @Nonnull AccountClose getReverse() {
        assert isOnClient() : "This method is called on a client.";
        
        return new AccountClose(getRole(), null);
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
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AccountInitialize(entity, signature, recipient, block);
        }
        
    }
    
}
