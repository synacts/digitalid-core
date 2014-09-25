package ch.xdf;

import ch.virtualid.annotations.Exposed;
import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.exceptions.InvalidEncodingException;
import javax.annotation.Nonnull;

/**
 * Wraps a block with the syntactic type {@code empty@xdf.ch} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class EmptyWrapper extends BlockWrapper implements Immutable {
    
    /**
     * Stores the syntactic type {@code empty@xdf.ch}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("empty@xdf.ch").load(0);
    
    /**
     * The byte length of nothing.
     * (Blocks may not have a length of zero.)
     */
    public static final int LENGTH = 1;
    
    
    /**
     * Encodes nothing into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     */
    public EmptyWrapper(@Nonnull SemanticType type) {
        super(type);
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    public EmptyWrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        if (block.getLength() != LENGTH) throw new InvalidEncodingException("The block's length is invalid.");
    }
    
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected int determineLength() {
        return LENGTH;
    }
    
    @Pure
    @Override
    protected void encode(@Exposed @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
    }
    
}
