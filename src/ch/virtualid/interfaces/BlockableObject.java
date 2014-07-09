package ch.virtualid.interfaces;

import ch.virtualid.annotation.Pure;
import ch.xdf.Block;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class implements the blocking mechanism to be inherited.
 * 
 * TODO: Keeping the block alive might be a problem for garbage collection, as it might be encoded into a big byte array, which cannot be released.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class BlockableObject implements Blockable, Immutable {
    
    /**
     * Stores the cached block.
     * 
     * @invariant block == null || block.getType().isBasedOn(getType()) : "The block is either null or based on the indicated type.";
     */
    private @Nullable Block block;
    
    /**
     * Creates a new blockable object without a block.
     */
    protected BlockableObject() {
        this.block = null;
    }
    
    /**
     * Creates a new blockable object with the given block.
     * 
     * @param block the block that encodes this object.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    protected BlockableObject(@Nonnull Block block) {
        assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
        
        this.block = block;
    }
    
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        if (block == null) block = encode();
        return block;
    }
    
    
    /**
     * Encodes the data of this object into a block.
     * 
     * @return a block that encodes the data of this object.
     * 
     * @ensure return.getType().isBasedOn(getType()) : "The returned block is based on the indicated type.";
     */
    @Pure
    protected abstract @Nonnull Block encode();
    
}
