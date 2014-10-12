package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models a list of {@link Predecessor predecessors}.
 * 
 * @invariant isFrozen() : "This list of predecessors is frozen.";
 * @invariant doesNotContainNull() : "This list of predecessors does not contain null.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Predecessors extends FreezableArrayList<Predecessor> implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code list.predecessor.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("list.predecessor.identity@virtualid.ch").load(ListWrapper.TYPE, Predecessor.TYPE);
    
    
    /**
     * Creates a new list of predecessors with the given identifier.
     * 
     * @param identifier the identifier whose predecessors are to be loaded.
     */
    public Predecessors(@Nonnull NonHostIdentifier identifier) throws SQLException {
        final @Nonnull ReadonlyList<NonHostIdentifier> predecessors = Mapper.getPredecessors(identifier);
        for (final @Nonnull NonHostIdentifier predecessor : predecessors) {
            add(new Predecessor(predecessor));
        }
        freeze();
    }
    
    /**
     * Creates a new list of predecessors from the given block.
     * 
     * @param block the block containing the list of predecessors.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public Predecessors(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyList<Block> predecessors = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block predecessor : predecessors) {
            add(new Predecessor(predecessor));
        }
        freeze();
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableList<Block> predecessors = new FreezableArrayList<Block>(size());
        for (final @Nonnull Predecessor predecessor : this) {
            predecessors.add(predecessor.toBlock());
        }
        return new ListWrapper(TYPE, predecessors.freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("[");
        for (final @Nonnull Predecessor predecessor : this) {
            if (string.length() > 1) string.append(", ");
            string.append(predecessor);
        }
        return string.append("]").toString();
    }
    
}
