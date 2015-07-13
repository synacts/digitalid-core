package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * This class implements the bare blockable mechanism.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class BlockableObject implements Blockable {
    
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
