package ch.virtualid.cryptography;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.DataWrapper;
import ch.xdf.EncryptionWrapper;
import java.security.SecureRandom;
import javax.annotation.Nonnull;
import javax.crypto.spec.IvParameterSpec;

/**
 * The random initialization vector ensures that multiple {@link EncryptionWrapper encryptions} of the same {@Block block} are different.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class InitializationVector extends IvParameterSpec implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code initialization.vector@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("initialization.vector@virtualid.ch").load(DataWrapper.TYPE);
    
    
    /**
     * Returns an array of 16 random bytes.
     * 
     * @return an array of 16 random bytes.
     * 
     * @ensure return.length == 16 : "The array contains 16 bytes.";
     */
    private static byte[] getRandomBytes() {
        final @Nonnull byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return  bytes;
    }
    
    /**
     * Creates a new random initialization vector.
     */
    public InitializationVector() {
        super(getRandomBytes());
    }
    
    /**
     * Creates a new initialization vector from the given block.
     * 
     * @param block the block containing the initialization vector.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public InitializationVector(@Nonnull Block block) throws InvalidEncodingException {
        super(new DataWrapper(block).getData());
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        if (block.getLength() != 17) throw new InvalidEncodingException("An initialization vector has to be 16 bytes long.");
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new DataWrapper(TYPE, getIV()).toBlock();
    }
    
}
