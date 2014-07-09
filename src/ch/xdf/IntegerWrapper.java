package ch.xdf;

import ch.virtualid.annotation.Exposed;
import ch.virtualid.annotation.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.exceptions.InvalidEncodingException;
import java.math.BigInteger;
import javax.annotation.Nonnull;

/**
 * Wraps a block with the syntactic type {@code integer@xdf.ch} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class IntegerWrapper extends BlockWrapper implements Immutable {
    
    /**
     * Stores the syntactic type {@code integer@xdf.ch}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("integer@xdf.ch").load(0);
    
    
    /**
     * Stores the value as a byte array.
     */
    private final @Nonnull byte[] bytes;
    
    /**
     * Stores the value of this wrapper.
     */
    private final @Nonnull BigInteger value;
    
    /**
     * Encodes the given value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     */
    public IntegerWrapper(@Nonnull SemanticType type, @Nonnull BigInteger value) {
        super(type);
        
        this.bytes = value.toByteArray();
        this.value = value;
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    public IntegerWrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        this.bytes = block.getBytes();
        this.value = new BigInteger(bytes);
    }
    
    /**
     * Returns the value of the wrapped block.
     * 
     * @return the value of the wrapped block.
     */
    @Pure
    public @Nonnull BigInteger getValue() {
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
        return bytes.length;
    }
    
    @Pure
    @Override
    protected void encode(@Exposed @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        block.setBytes(0, bytes);
    }
    
}
