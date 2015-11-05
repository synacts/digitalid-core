package net.digitalid.service.core.block.wrappers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.Encoding;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueEncodingFactory;
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueStoringFactory;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;

/**
 * This class wraps a {@code byte} for encoding and decoding a block of the syntactic type {@code int8@core.digitalid.net}.
 */
@Immutable
public final class Int8Wrapper extends Wrapper<Int8Wrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code int8@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("int8@core.digitalid.net").load(0);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value of this wrapper.
     */
    private final byte value;
    
    /**
     * Returns the value of this wrapper.
     * 
     * @return the value of this wrapper.
     */
    @Pure
    public byte getValue() {
        return value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new wrapper with the given type and value.
     * 
     * @param type the semantic type of the new wrapper.
     * @param value the value of the new wrapper.
     */
    private Int8Wrapper(@Nonnull @Loaded @BasedOn("int8@core.digitalid.net") SemanticType type, byte value) {
        super(type);
        
        this.value = value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Encodes the given value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new block containing the given value.
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("int8@core.digitalid.net") SemanticType type, byte value) {
        return new EncodingFactory(type).encodeNonNullable(new Int8Wrapper(type, value));
    }
    
    /**
     * Decodes the given block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     */
    @Pure
    public static byte decode(@Nonnull @NonEncoding @BasedOn("int8@core.digitalid.net") Block block) throws InvalidEncodingException {
        return new EncodingFactory(block.getType()).decodeNonNullable(None.OBJECT, block).value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The byte length of an int8.
     */
    public static final int LENGTH = 1;
    
    @Pure
    @Override
    public int determineLength() {
        return LENGTH;
    }
    
    @Pure
    @Override
    public void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        block.encodeValue(value);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory extends Wrapper.NonRequestingEncodingFactory<Int8Wrapper> {
        
        /**
         * Creates a new encoding factory with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private EncodingFactory(@Nonnull @BasedOn("int8@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull Int8Wrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("int8@core.digitalid.net") Block block) throws InvalidEncodingException {
            if (block.getLength() != LENGTH) throw new InvalidEncodingException("The block's length is invalid.");
            
            return new Int8Wrapper(getType(), (byte) block.decodeValue());
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
    public static final class StoringFactory extends Wrapper.StoringFactory<Int8Wrapper> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("value", SQLType.TINYINT);
        
        /**
         * Creates a new storing factory with the given type.
         * 
         * @param type the semantic type of the restored wrappers.
         */
        private StoringFactory(@Nonnull @Loaded @BasedOn("int8@core.digitalid.net") SemanticType type) {
            super(COLUMN, type);
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull Int8Wrapper wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setByte(parameterIndex, wrapper.value);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable Int8Wrapper restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final byte value = resultSet.getByte(columnIndex);
            if (resultSet.wasNull()) return null;
            else return new Int8Wrapper(getType(), value);
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
        return String.valueOf(value);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this wrapper.
     */
    @Immutable
    public static class Factory extends ValueWrapper.Factory<Byte, Int8Wrapper> {
        
        @Pure
        @Override
        protected @Nonnull Int8Wrapper wrap(@Nonnull SemanticType type, @Nonnull Byte value) {
            return new Int8Wrapper(type, value);
        }
        
        @Pure
        @Override
        protected @Nonnull Byte unwrap(@Nonnull Int8Wrapper wrapper) {
            return wrapper.value;
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    private static final @Nonnull Factory FACTORY = new Factory();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Converters –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the value encoding factory of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value encoding factory of this wrapper.
     */
    @Pure
    public static @Nonnull ValueEncodingFactory<Byte, Int8Wrapper> getValueEncodingFactory(@Nonnull @BasedOn("int8@core.digitalid.net") SemanticType type) {
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
    public static @Nonnull ValueStoringFactory<Byte, Int8Wrapper> getValueStoringFactory(@Nonnull @BasedOn("int8@core.digitalid.net") SemanticType type) {
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
    public static @Nonnull Converters<Byte, Object> getValueFactories(@Nonnull @BasedOn("int8@core.digitalid.net") SemanticType type) {
        return Converters.get(getValueEncodingFactory(type), getValueStoringFactory(type));
    }
    
}
