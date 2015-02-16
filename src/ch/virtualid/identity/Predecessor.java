package ch.virtualid.identity;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.collections.FreezableArray;
import ch.virtualid.collections.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a predecessor of an identifier.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Predecessor implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code predecessor.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("predecessor.identity@virtualid.ch");
    
    /**
     * Stores the semantic type {@code list.predecessor.identity@virtualid.ch}.
     */
    static final @Nonnull SemanticType PREDECESSORS = SemanticType.create("list.predecessor.identity@virtualid.ch").load(ListWrapper.TYPE, TYPE);
    
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
    private final @Nonnull ReadonlyPredecessors predecessors;
    
    /**
     * Creates a new predecessor with the given identifier and predecessors.
     * 
     * @param identifier the identifier of this predecessor.
     * @param predecessors the predecessors of this predecessor.
     * 
     * @require predecessors.isFrozen() : "The predecessors are frozen.";
     */
    public Predecessor(@Nonnull NonHostIdentifier identifier, @Nonnull ReadonlyPredecessors predecessors) {
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
        this(identifier, identifier instanceof InternalNonHostIdentifier ? Predecessors.get((InternalNonHostIdentifier) identifier) : new Predecessors().freeze());
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
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        this.identifier = IdentifierClass.create(elements.getNotNull(0)).toNonHostIdentifier();
        this.predecessors = new Predecessors(elements.getNotNull(1)).freeze();
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(2);
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
    public @Nonnull ReadonlyPredecessors getPredecessors() {
        return predecessors;
    }
    
    
    /**
     * Returns the identity of this predecessor or null if none of its predecessors (including itself) is mapped.
     * 
     * @return the identity of this predecessor or null if none of its predecessors (including itself) is mapped.
     */
    @NonCommitting
    @Nullable NonHostIdentity getIdentity() throws SQLException, IOException, PacketException, ExternalException {
        if (identifier.isMapped()) return identifier.getMappedIdentity();
        if (predecessors.getIdentities().isNotEmpty()) return identifier.getIdentity().toNonHostIdentity();
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
