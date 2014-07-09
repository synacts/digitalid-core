package ch.xdf;

import ch.virtualid.annotation.Exposed;
import ch.virtualid.annotation.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Blocks are wrapped by separate objects for modular decoding and encoding.
 *
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class BlockWrapper implements Blockable, Immutable {
    
    /**
     * References the wrapped block.
     */
    private final @Nonnull Block block;
    
    /**
     * Creates and wraps a new block for lazy encoding.
     * 
     * @param type the semantic type of the new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     */
    protected BlockWrapper(@Nonnull SemanticType type) {
        assert type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
        
        block = new Block(type, this);
    }
    
    /**
     * Wraps the given block.
     * 
     * @param block the block to wrap.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    protected BlockWrapper(@Nonnull Block block) {
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        this.block = block;
    }
    
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return block.getType();
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        return block;
    }
    
    
    /**
     * Returns the syntactic type that corresponds to this class.
     * 
     * @return the syntactic type that corresponds to this class.
     * 
     * @ensure return.isLoaded() : "The type declaration is loaded.";
     */
    @Pure
    public abstract @Nonnull SyntacticType getSyntacticType();
    
    /**
     * Determines the length of the wrapped block.
     * This method is needed for lazy encoding.
     * 
     * @return the length of the wrapped block.
     * 
     * @ensure determineLength() > 0 : "The length is positive (i.e. the block is not empty).";
     */
    @Pure
    protected abstract int determineLength();
    
    /**
     * Encodes the data into the wrapped block.
     * This method is needed for lazy encoding.
     * <p>
     * <em>Important:</em> Do not leak the given block!
     * 
     * @param block an exposed block to encode the data into.
     * 
     * @require block.isEncoding() : "The given block is in the process of being encoded.";
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     * @require block.getLength() == determineLength() : "The block's length has to match the determined length.";
     */
    protected abstract void encode(@Exposed @Nonnull Block block);
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof BlockWrapper)) return false;
        final @Nonnull BlockWrapper other = (BlockWrapper) object;
        return this.getClass().equals(other.getClass()) && this.block.equals(other.block);
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return block.hashCode();
    }
    
}
