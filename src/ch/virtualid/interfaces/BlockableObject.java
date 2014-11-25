package ch.virtualid.interfaces;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import javax.annotation.Nonnull;

/**
 * This class implements the bare blockable mechanism.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class BlockableObject implements Blockable, Immutable {
    
    /**
     * Stores the block of this object.
     */
    private final @Nonnull Block block;
    
    /**
     * Creates a new blockable object.
     * 
     * @param block the block to wrap.
     */
    public BlockableObject(@Nonnull Block block) {
        this.block = block;
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return block.getType();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return block;
    }
    
}
