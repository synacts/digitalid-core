package net.digitalid.service.core.identity;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identifier.IdentifierClass;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.identifier.NonHostIdentifier;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.Blockable;
import net.digitalid.service.core.wrappers.ListWrapper;
import net.digitalid.service.core.wrappers.TupleWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models a predecessor of an identifier.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class Predecessor implements Blockable {
    
    /**
     * Stores the semantic type {@code predecessor.identity@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("predecessor.identity@core.digitalid.net");
    
    /**
     * Stores the semantic type {@code list.predecessor.identity@core.digitalid.net}.
     */
    static final @Nonnull SemanticType PREDECESSORS = SemanticType.map("list.predecessor.identity@core.digitalid.net").load(ListWrapper.TYPE, TYPE);
    
    // Load the recursive declaration of the predecessor type.
    static { TYPE.load(TupleWrapper.TYPE, NonHostIdentity.IDENTIFIER, PREDECESSORS); }
    
    
    /**
     * Stores the identifier of this predecessor.
     */
    private final @Nonnull NonHostIdentifier identifier;
    
    /**
     * Stores the predecessors of this predecessor.
     * 
     * @invariant predecessors.isFrozen() : "The predecessors are frozen.";
     */
    private final @Nonnull ReadOnlyPredecessors predecessors;
    
    /**
     * Creates a new predecessor with the given identifier and predecessors.
     * 
     * @param identifier the identifier of this predecessor.
     * @param predecessors the predecessors of this predecessor.
     * 
     * @require predecessors.isFrozen() : "The predecessors are frozen.";
     */
    public Predecessor(@Nonnull NonHostIdentifier identifier, @Nonnull ReadOnlyPredecessors predecessors) {
        assert predecessors.isFrozen() : "The predecessors are frozen.";
        
        this.identifier = identifier;
        this.predecessors = predecessors;
    }
    
    /**
     * Creates a new predecessor with the given identifier.
     * This constructor loads the predecessors on its own.
     * 
     * @param identifier the identifier of this predecessor.
     */
    @NonCommitting
    public Predecessor(@Nonnull NonHostIdentifier identifier) throws SQLException {
        this(identifier, identifier instanceof InternalNonHostIdentifier ? FreezablePredecessors.get((InternalNonHostIdentifier) identifier) : new FreezablePredecessors().freeze());
    }
    
    /**
     * Creates a new predecessor from the given block.
     * 
     * @param block the block containing the predecessor.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public Predecessor(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(block).getNonNullableElements(2);
        this.identifier = IdentifierClass.create(elements.getNonNullable(0)).toNonHostIdentifier();
        this.predecessors = new FreezablePredecessors(elements.getNonNullable(1)).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<>(2);
        elements.set(0, identifier.toBlock());
        elements.set(1, predecessors.toBlock());
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return identifier.getString() + ": " + predecessors.toString();
    }
    
    
    /**
     * Returns the identifier of this predecessor.
     * 
     * @return the identifier of this predecessor.
     */
    public @Nonnull NonHostIdentifier getIdentifier() {
        return identifier;
    }
    
    /**
     * Returns the predecessors of this predecessor.
     * 
     * @return the predecessors of this predecessor.
     * 
     * @ensure return.isFrozen() : "The predecessors are frozen.";
     */
    public @Nonnull ReadOnlyPredecessors getPredecessors() {
        return predecessors;
    }
    
    
    /**
     * Returns the identity of this predecessor or null if none of its predecessors (including itself) is mapped.
     * 
     * @return the identity of this predecessor or null if none of its predecessors (including itself) is mapped.
     */
    @Pure
    @NonCommitting
    @Nullable NonHostIdentity getIdentity() throws SQLException, IOException, PacketException, ExternalException {
        if (identifier.isMapped()) return identifier.getMappedIdentity();
        if (!predecessors.getIdentities().isEmpty()) return identifier.getIdentity().toNonHostIdentity();
        return null;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Predecessor)) return false;
        final @Nonnull Predecessor other = (Predecessor) object;
        
        return this.identifier.equals(other.identifier) && this.predecessors.equals(other.predecessors);
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + identifier.hashCode();
        hash = 89 * hash + predecessors.hashCode();
        return hash;
    }
    
}
