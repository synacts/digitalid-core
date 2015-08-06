package net.digitalid.core.wrappers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.BasedOn;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEmpty;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.NonNegative;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.database.Column;
import net.digitalid.core.database.SQLType;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;

/**
 * Wraps values of the syntactic type {@code intvar@core.digitalid.net} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class IntvarWrapper extends Wrapper<IntvarWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code intvar@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("intvar@core.digitalid.net").load(0);
    
    /**
     * Stores the semantic type {@code semantic.intvar@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.intvar@core.digitalid.net").load(TYPE);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the maximum value an intvar can have.
     */
    public static final long MAX_VALUE = 4611686018427387903l;
    
    /**
     * Stores the value of this wrapper.
     * 
     * @invariant value <= MAX_VALUE : "The first two bits are zero.";
     */
    private final @NonNegative long value;
    
    /**
     * Returns the value of this wrapper.
     * 
     * @return the value of this wrapper.
     * 
     * @ensure value <= MAX_VALUE : "The first two bits are zero.";
     */
    @Pure
    public @NonNegative long getValue() {
        return value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new wrapper with the given type and value.
     * 
     * @param type the semantic type of the new wrapper.
     * @param value the value of the new wrapper.
     * 
     * @require value <= MAX_VALUE : "The first two bits have to be zero.";
     */
    private IntvarWrapper(@Nonnull @Loaded @BasedOn("intvar@core.digitalid.net") SemanticType type, @NonNegative long value) {
        super(type);
        
        assert value >= 0 : "The value is non-negative.";
        assert value <= MAX_VALUE : "The first two bits have to be zero.";
        
        this.value = value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory of this class.
     */
    private static final Factory FACTORY = new Factory(SEMANTIC);
    
    /**
     * Encodes the given value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new block containing the given value.
     * 
     * @require value <= MAX_VALUE : "The first two bits have to be zero.";
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("intvar@core.digitalid.net") SemanticType type, @NonNegative long value) {
        return FACTORY.encodeNonNullable(new IntvarWrapper(type, value));
    }
    
    /**
     * Decodes the given block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     * 
     * @ensure value <= MAX_VALUE : "The first two bits are zero.";
     */
    @Pure
    public static @NonNegative long decode(@Nonnull @NonEncoding @BasedOn("intvar@core.digitalid.net") Block block) throws InvalidEncodingException {
        return FACTORY.decodeNonNullable(block).value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    protected int determineLength() {
        return determineLength(value);
    }
    
    @Pure
    @Override
    protected void encode(@Encoding @Nonnull Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        encode(block, 0, block.getLength(), value);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decode Length –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Decodes the length of the intvar as indicated in the first two bits of the byte at the given offset.
     * 
     * @param block the block containing the intvar.
     * @param offset the offset of the intvar in the block.
     * 
     * @return the length of the intvar.
     * 
     * @ensure return == 1 || return == 2 || return == 4 || return == 8 : "The result is either 1, 2, 4 or 8.";
     */
    @Pure
    public static int decodeLength(@Nonnull Block block, @NonNegative int offset) throws InvalidEncodingException {
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
     * @ensure return == 1 || return == 2 || return == 4 || return == 8 : "The result is either 1, 2, 4 or 8.";
     */
    @Pure
    static int decodeLength(@Nonnull @NonEmpty byte[] bytes) throws InvalidEncodingException {
        assert bytes.length > 0 : "The byte array is not empty.";
        
        return 1 << ((bytes[0] & 0xFF) >>> 6);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decode Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Decodes the value of the intvar that is stored in the indicated section of the given block.
     * 
     * @param block the block containing the value.
     * @param offset the offset of the intvar in the block.
     * @param length the length of the intvar in the block.
     * 
     * @return the decoded value of the intvar.
     * 
     * @require length == decodeLength(block, offset) : "The length is correct.";
     * 
     * @ensure determineLength(return) == length : "The length of the return value as an intvar matches the given length.";
     * @ensure value <= MAX_VALUE : "The first two bits are zero.";
     */
    @Pure
    public static @NonNegative long decodeValue(@Nonnull Block block, @NonNegative int offset, int length) throws InvalidEncodingException {
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
     * @ensure value <= MAX_VALUE : "The first two bits are zero.";
     */
    @Pure
    static @NonNegative long decodeValue(@Nonnull byte[] bytes, int length) throws InvalidEncodingException {
        assert bytes.length >= length : "The byte array is big enough.";
        assert length == decodeLength(bytes) : "The length is correct.";
        
        long result = bytes[0] & 0x3F;
        for (int i = 1; i < length; i++) {
            result = (result << 8) | (bytes[i] & 0xFF);
        }
        
        if (determineLength(result) != length) throw new InvalidEncodingException("The length of the return value as an intvar has to match the given length.");
        
        return result;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Static Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Determines the length of the given value when encoded as an intvar.
     * 
     * @param value the value to be encoded as an intvar.
     * 
     * @return the length of the given value when encoded as an intvar.
     * 
     * @require value <= MAX_VALUE : "The first two bits have to be zero.";
     */
    @Pure
    public static int determineLength(@NonNegative long value) {
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
     * @require offset + length <= block.getLength() : "The indicated section may not exceed the given block.";
     * @require length == determineLength(value) : "The length of the indicated section in the block has to match the length of the encoded value.";
     * @require value <= MAX_VALUE : "The first two bits have to be zero.";
     */
    public static void encode(@Nonnull @Encoding Block block, @NonNegative int offset, int length, @NonNegative long value) {
        assert offset >= 0 : "The offset is not negative.";
        assert offset + length <= block.getLength() : "The indicated section may not exceed the given block.";
        assert length == determineLength(value) : "The length of the indicated section in the block has to match the length of the encoded value.";
        assert value >= 0 : "The value is not negative.";
        assert value <= MAX_VALUE : "The first two bits have to be zero.";
        
        long shifter = value;
        for (int i = length - 1; i >= 1; i--) {  
            block.setByte(offset + i, (byte) shifter);
            shifter >>>= 8;
        }
        
        block.setByte(offset, (byte) (shifter | (Integer.numberOfTrailingZeros(length) << 6)));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static class Factory extends Wrapper.Factory<IntvarWrapper> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("value", SQLType.BIGINT);
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        private Factory(@Nonnull @Loaded @BasedOn("intvar@core.digitalid.net") SemanticType type) {
            super(type, COLUMN);
            
            assert type.isBasedOn(TYPE) : "The given semantic type is based on the indicated syntactic type.";
        }
        
        @Pure
        @Override
        public @Nonnull IntvarWrapper decodeNonNullable(@Nonnull @NonEncoding Block block) throws InvalidEncodingException {
            final int length = block.getLength();
            
            if (length != decodeLength(block, 0)) throw new InvalidEncodingException("The block's length is invalid.");
            
            final long value = decodeValue(block, 0, length);
            return new IntvarWrapper(block.getType(), value);
        }
        
        @Override
        @NonCommitting
        public void setNonNullable(@Nonnull IntvarWrapper wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setLong(parameterIndex, wrapper.value);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable IntvarWrapper getNullable(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final long value = resultSet.getLong(columnIndex);
            if (resultSet.wasNull()) return null;
            if (value < 0 || value > MAX_VALUE) throw new SQLException("The value " + value + " does not fit into an intvar.");
            else return new IntvarWrapper(getType(), value);
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull Factory getFactory() {
        return new Factory(getSemanticType());
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(value);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for the value type of this wrapper.
     */
    @Immutable
    public static class ValueFactory extends Wrapper.ValueFactory<Long, IntvarWrapper> {
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the type of the blocks which are returned by the factory.
         */
        private ValueFactory(@Nonnull @Loaded @BasedOn("intvar@core.digitalid.net") SemanticType type) {
            super(type, FACTORY);
            
            assert type.isBasedOn(TYPE) : "The given semantic type is based on the indicated syntactic type.";
        }
        
        @Pure
        @Override
        protected boolean isValid(@Nonnull Long value) {
            return value >= 0 && value <= MAX_VALUE;
        }
        
        @Pure
        @Override
        protected @Nonnull IntvarWrapper wrap(@Nonnull Long value) {
            assert isValid(value) : "The value is valid.";
            
            return new IntvarWrapper(getType(), value);
        }
        
        @Pure
        @Override
        protected @Nonnull Long unwrap(@Nonnull IntvarWrapper wrapper) {
            return wrapper.value;
        }
        
    }
    
    /**
     * Returns a new factory for the value type of this wrapper.
     * 
     * @param type the type of the blocks which are returned by the factory.
     * 
     * @return a new factory for the value type of this wrapper.
     */
    @Pure
    public static @Nonnull ValueFactory getValueFactory(@Nonnull @Loaded @BasedOn("intvar@core.digitalid.net") SemanticType type) {
        return new ValueFactory(type);
    }
    
    /**
     * Stores the factory for the value type of this wrapper.
     */
    public static final @Nonnull ValueFactory VALUE_FACTORY = new ValueFactory(SEMANTIC);
    
}
