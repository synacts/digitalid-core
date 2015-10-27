package net.digitalid.service.core.block.wrappers;

import net.digitalid.service.core.block.annotations.Encoding;
import net.digitalid.service.core.block.annotations.NonEncoding;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueEncodingFactory;
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueStoringFactory;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.factory.Factories;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;

/**
 * This class wraps an {@link BigInteger integer} for encoding and decoding a block of the syntactic type {@code integer@core.digitalid.net}.
 */
@Immutable
public final class IntegerWrapper extends Wrapper<IntegerWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code integer@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("integer@core.digitalid.net").load(0);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value as a byte array.
     */
    private final @Nonnull byte[] bytes;
    
    /**
     * Stores the value of this wrapper.
     */
    private final @Nonnull BigInteger value;
    
    /**
     * Returns the value of this wrapper.
     * 
     * @return the value of this wrapper.
     */
    @Pure
    public @Nonnull BigInteger getValue() {
        return value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new wrapper with the given type and value.
     * 
     * @param type the semantic type of the new wrapper.
     * @param bytes the value as a byte array.
     * @param value the value of the new wrapper.
     */
    private IntegerWrapper(@Nonnull @Loaded @BasedOn("integer@core.digitalid.net") SemanticType type, @Nonnull byte[] bytes, @Nonnull BigInteger value) {
        super(type);
        
        this.bytes = bytes;
        this.value = value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Encodes the given value into a new non-nullable block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new non-nullable block containing the given value.
     */
    @Pure
    public static @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull @Loaded @BasedOn("integer@core.digitalid.net") SemanticType type, @Nonnull BigInteger value) {
        return new EncodingFactory(type).encodeNonNullable(new IntegerWrapper(type, value.toByteArray(), value));
    }
    
    /**
     * Encodes the given value into a new nullable block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new nullable block containing the given value.
     */
    @Pure
    public static @Nullable @NonEncoding Block encodeNullable(@Nonnull @Loaded @BasedOn("integer@core.digitalid.net") SemanticType type, @Nullable BigInteger value) {
        return value == null ? null : encodeNonNullable(type, value);
    }
    
    /**
     * Decodes the given non-nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     */
    @Pure
    public static @Nonnull BigInteger decodeNonNullable(@Nonnull @NonEncoding @BasedOn("integer@core.digitalid.net") Block block) throws InvalidEncodingException {
        return new EncodingFactory(block.getType()).decodeNonNullable(None.OBJECT, block).value;
    }
    
    /**
     * Decodes the given nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     */
    @Pure
    public static @Nullable BigInteger decodeNullable(@Nullable @NonEncoding @BasedOn("integer@core.digitalid.net") Block block) throws InvalidEncodingException {
        return block == null ? null : decodeNonNullable(block);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    protected int determineLength() {
        return bytes.length;
    }
    
    @Pure
    @Override
    protected void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        block.setBytes(0, bytes);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory extends Wrapper.NonRequestingEncodingFactory<IntegerWrapper> {
        
        /**
         * Creates a new encoding factory with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private EncodingFactory(@Nonnull @BasedOn("integer@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull IntegerWrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("integer@core.digitalid.net") Block block) throws InvalidEncodingException {
            final byte[] bytes = block.getBytes();
            return new IntegerWrapper(block.getType(), bytes, new BigInteger(bytes));
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull EncodingFactory getEncodingFactory() {
        return new EncodingFactory(getSemanticType());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The storing factory for this class.
     */
    @Immutable
    public static final class StoringFactory extends Wrapper.StoringFactory<IntegerWrapper> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("value", SQLType.BLOB);
        
        /**
         * Creates a new storing factory with the given type.
         * 
         * @param type the semantic type of the restored wrappers.
         */
        private StoringFactory(@Nonnull @Loaded @BasedOn("integer@core.digitalid.net") SemanticType type) {
            super(COLUMN, type);
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull IntegerWrapper wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setBytes(parameterIndex, wrapper.bytes);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable IntegerWrapper restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable byte[] bytes = resultSet.getBytes(columnIndex);
            return bytes == null ? null : new IntegerWrapper(getType(), bytes, new BigInteger(bytes));
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull StoringFactory getStoringFactory() {
        return new StoringFactory(getSemanticType());
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Block.toString(bytes);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this wrapper.
     */
    @Immutable
    public static class Factory extends ValueWrapper.Factory<BigInteger, IntegerWrapper> {
        
        @Pure
        @Override
        protected @Nonnull IntegerWrapper wrap(@Nonnull SemanticType type, @Nonnull BigInteger value) {
            return new IntegerWrapper(type, value.toByteArray(), value);
        }
        
        @Pure
        @Override
        protected @Nonnull BigInteger unwrap(@Nonnull IntegerWrapper wrapper) {
            return wrapper.value;
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    private static final @Nonnull Factory FACTORY = new Factory();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the value encoding factory of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value encoding factory of this wrapper.
     */
    @Pure
    public static @Nonnull ValueEncodingFactory<BigInteger, IntegerWrapper> getValueEncodingFactory(@Nonnull @BasedOn("integer@core.digitalid.net") SemanticType type) {
        return new ValueEncodingFactory<>(FACTORY, new EncodingFactory(type));
    }
    
    /**
     * Returns the value storing factory of this wrapper.
     * 
     * @param type any semantic type that is based on the syntactic type of this wrapper.
     * 
     * @return the value storing factory of this wrapper.
     */
    @Pure
    public static @Nonnull ValueStoringFactory<BigInteger, IntegerWrapper> getValueStoringFactory(@Nonnull @BasedOn("integer@core.digitalid.net") SemanticType type) {
        return new ValueStoringFactory<>(FACTORY, new StoringFactory(type));
    }
    
    /**
     * Returns the value factories of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value factories of this wrapper.
     */
    @Pure
    public static @Nonnull Factories<BigInteger, Object> getValueFactories(@Nonnull @BasedOn("integer@core.digitalid.net") SemanticType type) {
        return Factories.get(getValueEncodingFactory(type), getValueStoringFactory(type));
    }
    
}
