package ch.xdf;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Captured;
import ch.virtualid.annotations.Exposed;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Immutable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a block with the syntactic type {@code data@xdf.ch} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class DataWrapper extends BlockWrapper implements Immutable {
    
    /**
     * Stores the syntactic type {@code data@xdf.ch}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("data@xdf.ch").load(0);
    
    
    /**
     * Stores the data of this wrapper.
     */
    private final @Nullable byte[] data;
    
    /**
     * Encodes the given data into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param data the data to encode into the new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     */
    public DataWrapper(@Nonnull SemanticType type, @Captured @Nonnull byte[] data) {
        super(type);
        
        this.data = data;
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    public DataWrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        this.data = null;
    }
    
    /**
     * Returns the data of the wrapped block.
     * 
     * @return the data of the wrapped block.
     */
    @Pure
    public @Capturable @Nonnull byte[] getData() {
        if (data != null) return data.clone();
        else return toBlock().getBytes(1);
    }
    
    /**
     * Returns the data of the wrapped block as an input stream.
     * 
     * @return the data of the wrapped block as an input stream.
     */
    @Pure
    public @Nonnull InputStream getDataAsInputStream() {
        if (data != null) return new ByteArrayInputStream(data);
        else return toBlock().getInputStream(1);
    }
    
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected int determineLength() {
        if (data != null) return data.length + 1;
        else return toBlock().getLength();
    }
    
    @Pure
    @Override
    protected void encode(@Exposed @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        if (data != null) block.setBytes(1, data);
    }
    
}
