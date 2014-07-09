package ch.xdf;

import ch.virtualid.annotation.Exposed;
import ch.virtualid.annotation.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.exceptions.InvalidEncodingException;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;

/**
 * Wraps a block with the syntactic type {@code string@xdf.ch} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class StringWrapper extends BlockWrapper implements Immutable {
    
    /**
     * Stores the syntactic type {@code string@xdf.ch}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("string@xdf.ch").load(0);
    
    /**
     * Stores the character set used to encode and decode strings.
     */
    public static final @Nonnull Charset CHARSET = Charset.forName("UTF-16BE");
    
    
    /**
     * Stores the string as a byte array.
     */
    private final @Nonnull byte[] bytes;
    
    /**
     * Stores the string of this wrapper.
     */
    private final @Nonnull String string;
    
    /**
     * Encodes the given string into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param string the string to encode into the new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     */
    public StringWrapper(@Nonnull SemanticType type, @Nonnull String string) {
        super(type);
        
        this.bytes = string.getBytes(CHARSET);
        this.string = string;
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    public StringWrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        this.bytes = block.getBytes(1);
        this.string = new String(bytes, 0, bytes.length, CHARSET);
    }
    
    /**
     * Returns the string of the wrapped block.
     * 
     * @return the string of the wrapped block.
     */
    @Pure
    public @Nonnull String getString() {
        return string;
    }
    
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected int determineLength() {
        return bytes.length + 1;
    }
    
    @Pure
    @Override
    protected void encode(@Exposed @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        block.setBytes(1, bytes);
    }
    
}
