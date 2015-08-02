package net.digitalid.core.wrappers;

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;

/**
 * Wraps a block with the syntactic type {@code string@core.digitalid.net} for encoding and decoding.
 * <p>
 * <em>Important:</em> SQL injections have to be prevented by the caller of this class!
 * Only a warning is issued when the character might be used in a normal SQL statement.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class StringWrapper extends Wrapper {
    
    /**
     * Stores the syntactic type {@code string@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("string@core.digitalid.net").load(0);
    
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
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
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
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
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
    protected void encode(@Encoding @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        block.setBytes(1, bytes);
    }
    
}
