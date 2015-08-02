package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;

/**
 * Wraps a block with the syntactic type {@code int32@core.digitalid.net} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class Int32Wrapper extends Wrapper {
    
    /**
     * Stores the syntactic type {@code int32@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("int32@core.digitalid.net").load(0);
    
    /**
     * The byte length of an int32.
     */
    public static final int LENGTH = 4;
    
    
    /**
     * Stores the value of this wrapper.
     */
    private final int value;
    
    /**
     * Encodes the given value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     */
    public Int32Wrapper(@Nonnull SemanticType type, int value) {
        super(type);
        
        this.value = value;
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    public Int32Wrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        if (block.getLength() != LENGTH) throw new InvalidEncodingException("The block's length is invalid.");
        
        int value = 0;
        for (int i = 0; i < LENGTH; i++) {
            value = (value << 8) | (block.getByte(i) & 0xff);
        }
        this.value = value;
    }
    
    /**
     * Returns the value of the wrapped block.
     * 
     * @return the value of the wrapped block.
     */
    @Pure
    public int getValue() {
        return value;
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
    protected void encode(@Encoding @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        int value = this.value;
        for (int i = LENGTH - 1; i >= 0; i--) {  
            block.setByte(i, (byte) value);
            value >>>= 8;
        }
    }
    
}
