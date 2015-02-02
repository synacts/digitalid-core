package ch.xdf;

import ch.virtualid.annotations.Exposed;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * Wraps a block with the syntactic type {@code intvar@xdf.ch} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class IntvarWrapper extends BlockWrapper implements Immutable {
    
    /**
     * Stores the syntactic type {@code intvar@xdf.ch}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("intvar@xdf.ch").load(0);
    
    /**
     * Stores the maximum value an intvar can have.
     */
    public static final long MAX_VALUE = 4611686018427387903l;
    
    
    /**
     * Stores the value of this wrapper.
     * 
     * @invariant value >= 0 : "The value is not negative.";
     * @invariant value <= MAX_VALUE : "The first two bits are zero.";
     */
    private final long value;
    
    /**
     * Encodes the given value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(TYPE) : "The given type is based on the indicated syntactic type.";
     * @require value >= 0 : "The value is not negative.";
     * @require value <= MAX_VALUE : "The first two bits have to be zero.";
     */
    public IntvarWrapper(@Nonnull SemanticType type, long value) {
        super(type);
        
        assert value >= 0 : "The value is not negative.";
        assert value <= MAX_VALUE : "The first two bits have to be zero.";
        
        this.value = value;
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to wrap and decode.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated syntactic type.";
     */
    public IntvarWrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        final int length = block.getLength();
        
        if (length != decodeLength(block, 0)) throw new InvalidEncodingException("The block's length is invalid.");
        
        value = decodeValue(block, 0, length);
    }
    
    /**
     * Returns the value of the wrapped block.
     * 
     * @return the value of the wrapped block.
     * 
     * @ensure value >= 0 : "The value is not negative.";
     * @ensure value <= MAX_VALUE : "The first two bits are zero.";
     */
    @Pure
    public long getValue() {
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
        return determineLength(value);
    }
    
    @Pure
    @Override
    protected void encode(@Exposed @Nonnull Block block) {
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        encode(block, 0, block.getLength(), value);
    }
    
    
    /**
     * Decodes the length of the intvar as indicated in the first two bits of the byte at the given offset.
     * 
     * @param block the block containing the intvar.
     * @param offset the offset of the intvar in the block.
     * 
     * @return the length of the intvar.
     * 
     * @require offset >= 0 : "The offset is not negative.";
     * 
     * @ensure return == 1 || return == 2 || return == 4 || return == 8 : "The result is either 1, 2, 4 or 8.";
     */
    @Pure
    public static int decodeLength(@Nonnull Block block, int offset) throws InvalidEncodingException {
        assert offset >= 0 : "The offset is not negative.";
        
        if (offset >= block.getLength()) throw new InvalidEncodingException("The offset is not out of bounds.");
        
        return 1 << ((block.getByte(offset) & 0xFF) >>> 6);
    }
    
    /**
     * Decodes the length of the intvar as indicated in the first two bits of the first byte in the given byte array.
     * 
     * @param bytes the byte array containing the intvar at index 0.
     * 
     * @return the length of the intvar.
     * 
     * @require bytes.length > 0 : "The byte array is not empty.";
     * 
     * @ensure return == 1 || return == 2 || return == 4 || return == 8 : "The result is either 1, 2, 4 or 8.";
     */
    @Pure
    static int decodeLength(@Nonnull byte[] bytes) throws InvalidEncodingException {
        assert bytes.length > 0 : "The byte array is not empty.";
        
        return 1 << ((bytes[0] & 0xFF) >>> 6);
    }
    
    
    /**
     * Decodes the value of the intvar that is stored in the indicated section of the given block.
     * 
     * @param block the block containing the value.
     * @param offset the offset of the intvar in the block.
     * @param length the length of the intvar in the block.
     * 
     * @return the decoded value of the intvar.
     * 
     * @require offset >= 0 : "The offset is not negative.";
     * @require length == decodeLength(block, offset) : "The length is correct.";
     * 
     * @ensure determineLength(return) == length : "The length of the return value as an intvar matches the given length.";
     */
    @Pure
    public static long decodeValue(@Nonnull Block block, int offset, int length) throws InvalidEncodingException {
        assert offset >= 0 : "The offset is not negative.";
        assert length == decodeLength(block, offset) : "The length is correct.";
        
        if (offset + length > block.getLength()) throw new InvalidEncodingException("The indicated section may not exceed the block.");
        
        long result = block.getByte(offset) & 0x3F;
        for (int i = 1; i < length; i++) {
            result = (result << 8) | (block.getByte(offset + i) & 0xFF);
        }
        
        if (determineLength(result) != length) throw new InvalidEncodingException("The length of the return value as an intvar has to match the given length.");
        
        return result;
    }
    
    /**
     * Decodes the value of the intvar that is stored in the first bytes of the given byte array.
     * 
     * @param bytes the byte array containing the intvar at index 0.
     * @param length the length of the intvar in the given byte array.
     * 
     * @return the decoded value of the intvar.
     * 
     * @require bytes.length >= length : "The byte array is big enough.";
     * @require length == decodeLength(bytes) : "The length is correct.";
     * 
     * @ensure determineLength(return) == length : "The length of the return value as an intvar matches the given length.";
     */
    @Pure
    static long decodeValue(@Nonnull byte[] bytes, int length) throws InvalidEncodingException {
        assert bytes.length >= length : "The byte array is big enough.";
        assert length == decodeLength(bytes) : "The length is correct.";
        
        long result = bytes[0] & 0x3F;
        for (int i = 1; i < length; i++) {
            result = (result << 8) | (bytes[i] & 0xFF);
        }
        
        if (determineLength(result) != length) throw new InvalidEncodingException("The length of the return value as an intvar has to match the given length.");
        
        return result;
    }
    
    
    /**
     * Determines the length of the given value when encoded as an intvar.
     * 
     * @param value the value to be encoded as an intvar.
     * 
     * @return the length of the given value when encoded as an intvar.
     * 
     * @require value >= 0 : "The value is not negative.";
     * @require value <= MAX_VALUE : "The first two bits have to be zero.";
     */
    @Pure
    public static int determineLength(long value) {
        assert value >= 0 : "The value is not negative.";
        assert value <= MAX_VALUE : "The first two bits have to be zero.";
        
        if (value >= 1073741824) { // 2^30
            return 8;
        } else if (value >= 16384) { // 2^14
            return 4;
        } else if (value >= 64) { // 2^6
            return 2;
        } else {
            return 1;
        }
    }
    
    /**
     * Encodes the given value into the indicated section of the block.
     * 
     * @param block the block into which the value is encoded.
     * @param offset the offset of the indicated section in the block.
     * @param length the length of the indicated section in the block.
     * @param value the value to be encoded as an intvar.
     * 
     * @require block.isEncoding() : "The given block is in the process of being encoded.";
     * @require offset >= 0 : "The offset is not negative.";
     * @require offset + length <= block.getLength() : "The indicated section may not exceed the given block.";
     * @require length == determineLength(value) : "The length of the indicated section in the block has to match the length of the encoded value.";
     * @require value >= 0 : "The value is not negative.";
     * @require value <= MAX_VALUE : "The first two bits have to be zero.";
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    public static void encode(final @Exposed @Nonnull Block block, final int offset, final int length, long value) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert offset >= 0 : "The offset is not negative.";
        assert offset + length <= block.getLength() : "The indicated section may not exceed the given block.";
        assert length == determineLength(value) : "The length of the indicated section in the block has to match the length of the encoded value.";
        assert value >= 0 : "The value is not negative.";
        assert value <= MAX_VALUE : "The first two bits have to be zero.";
        
        for (int i = length - 1; i >= 1; i--){  
            block.setByte(offset + i, (byte) value);
            value >>>= 8;
        }
        
        block.setByte(offset, (byte) (value | (Integer.numberOfTrailingZeros(length) << 6)));
    }
    
}
