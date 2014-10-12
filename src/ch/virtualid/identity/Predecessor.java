package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.TupleWrapper;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models a predecessor of an identifier.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Predecessor implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code predecessor.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("predecessor.identity@virtualid.ch");
    
    // Load the recursive declaration of the predecessor type.
    static { TYPE.load(TupleWrapper.TYPE, NonHostIdentity.IDENTIFIER, Predecessors.TYPE); }
    
    
    /**
     * Stores the identifier of this predecessor.
     */
    private final @Nonnull NonHostIdentifier identifier;
    
    /**
     * Stores the predecessors of this predecessor.
     */
    private final @Nonnull Predecessors predecessors;
    
    /**
     * Creates a new predecessor with the given identifier.
     * 
     * @param identifier the identifier of this predecessor.
     */
    public Predecessor(@Nonnull NonHostIdentifier identifier) throws SQLException {
        this.identifier = identifier;
        this.predecessors = new Predecessors(identifier);
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
        this.identifier = new NonHostIdentifier(elements.getNotNull(0));
        this.predecessors = new Predecessors(elements.getNotNull(1));
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
     */
    public @Nonnull Predecessors getPredecessors() {
        return predecessors;
    }
    
}
