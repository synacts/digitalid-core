package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.Positive;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;
import net.digitalid.core.storable.Storable;

/**
 * Blocks are wrapped by separate objects for modular decoding and encoding.
 *
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class Wrapper<W extends Wrapper<W>> implements Storable<W> { // TODO: Probably declare the storable interface in the non-abstract wrappers.
    
    // TODO: Introduce a factory that implements @Nonnull Block encodeNonNullable(@Nonnull O object) with by return new Block(@Nonnull @Loaded SemanticType type, @Nonnull Wrapper<?> wrapper).
    
    /**
     * Stores the semantic type of this wrapper.
     */
    private final @Nonnull SemanticType type;
    
    /**
     * Creates and wraps a new block for lazy encoding.
     * 
     * @param type the semantic type of the new block.
     * 
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     */
    protected Wrapper(@Nonnull @Loaded SemanticType type) {
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
    protected Wrapper(@Nonnull Block block) {
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        this.block = block;
    }
    
    // TODO: Introduce a WrapperFactory (that uses the local type).
    
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
     */
    @Pure
    public abstract @Nonnull @Loaded SyntacticType getSyntacticType();
    
    /**
     * Determines the length of the wrapped block.
     * This method is needed for lazy encoding.
     * 
     * @return the length of the wrapped block.
     */
    @Pure
    protected abstract @Positive int determineLength();
    
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
    protected abstract void encode(@Encoding @Nonnull Block block);
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Wrapper)) return false;
        final @Nonnull Wrapper other = (Wrapper) object;
        return this.getClass().equals(other.getClass()) && this.block.equals(other.block);
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return block.hashCode();
    }
    
}
