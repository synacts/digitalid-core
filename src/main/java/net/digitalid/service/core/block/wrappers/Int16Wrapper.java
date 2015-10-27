package net.digitalid.service.core.block.wrappers;

import net.digitalid.service.core.block.annotations.Encoding;
import net.digitalid.service.core.block.annotations.NonEncoding;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.factory.Factories;
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
 * This class wraps a {@code short} for encoding and decoding a block of the syntactic type {@code int16@core.digitalid.net}.
 */
@Immutable
public final class Int16Wrapper extends ValueWrapper<Int16Wrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code int16@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("int16@core.digitalid.net").load(0);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value of this wrapper.
     */
    private final short value;
    
    /**
     * Returns the value of this wrapper.
     * 
     * @return the value of this wrapper.
     */
    @Pure
    public short getValue() {
        return value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new wrapper with the given type and value.
     * 
     * @param type the semantic type of the new wrapper.
     * @param value the value of the new wrapper.
     */
    private Int16Wrapper(@Nonnull @BasedOn("int16@core.digitalid.net") SemanticType type, short value) {
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
    public static @Nonnull @NonEncoding Block encode(@Nonnull @BasedOn("int16@core.digitalid.net") SemanticType type, short value) {
        return new EncodingFactory(type).encodeNonNullable(new Int16Wrapper(type, value));
    }
    
    /**
     * Decodes the given block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     */
    @Pure
    public static short decode(@Nonnull @NonEncoding @BasedOn("int16@core.digitalid.net") Block block) throws InvalidEncodingException {
        return new EncodingFactory(block.getType()).decodeNonNullable(None.OBJECT, block).value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The byte length of an int16.
     */
    public static final int LENGTH = 2;
    
    @Pure
    @Override
    protected int determineLength() {
        return LENGTH;
    }
    
    @Pure
    @Override
    protected void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        block.encodeValue(value);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory extends Wrapper.NonRequestingEncodingFactory<Int16Wrapper> {
        
        /**
         * Creates a new encoding factory with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private EncodingFactory(@Nonnull @BasedOn("int16@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull Int16Wrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("int16@core.digitalid.net") Block block) throws InvalidEncodingException {
            if (block.getLength() != LENGTH) throw new InvalidEncodingException("The block's length is invalid.");
            
            return new Int16Wrapper(getType(), (short) block.decodeValue());
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
    public static final class StoringFactory extends Wrapper.StoringFactory<Int16Wrapper> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("value", SQLType.SMALLINT);
        
        /**
         * Creates a new storing factory with the given type.
         * 
         * @param type the semantic type of the restored wrappers.
         */
        private StoringFactory(@Nonnull @BasedOn("int16@core.digitalid.net") SemanticType type) {
            super(COLUMN, type);
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull Int16Wrapper wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setShort(parameterIndex, wrapper.value);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable Int16Wrapper restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final short value = resultSet.getShort(columnIndex);
            return resultSet.wasNull() ? null : new Int16Wrapper(getType(), value);
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
    public static class Factory extends ValueWrapper.Factory<Short, Int16Wrapper> {
        
        @Pure
        @Override
        protected @Nonnull Int16Wrapper wrap(@Nonnull SemanticType type, @Nonnull Short value) {
            return new Int16Wrapper(type, value);
        }
        
        @Pure
        @Override
        protected @Nonnull Short unwrap(@Nonnull Int16Wrapper wrapper) {
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
    public static @Nonnull ValueEncodingFactory<Short, Int16Wrapper> getValueEncodingFactory(@Nonnull @BasedOn("int16@core.digitalid.net") SemanticType type) {
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
    public static @Nonnull ValueStoringFactory<Short, Int16Wrapper> getValueStoringFactory(@Nonnull @BasedOn("int16@core.digitalid.net") SemanticType type) {
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
    public static @Nonnull Factories<Short, Object> getValueFactories(@Nonnull @BasedOn("int16@core.digitalid.net") SemanticType type) {
        return Factories.get(getValueEncodingFactory(type), getValueStoringFactory(type));
    }
    
}
