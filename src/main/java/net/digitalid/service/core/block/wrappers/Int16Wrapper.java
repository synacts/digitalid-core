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
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;

/**
 * This class wraps a {@code short} for encoding and decoding a block of the syntactic type {@code int16@core.digitalid.net}.
 */
@Immutable
public final class Int16Wrapper extends ValueWrapper<Int16Wrapper> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Stores the syntactic type {@code int16@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("int16@core.digitalid.net").load(0);

    /**
     * Stores the syntactic type {@code semantic.int16@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.int16@core.digitalid.net").load(TYPE);

    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Utility -------------------------------------------------- */

    /**
     * Stores a static XDF converter for performance reasons.
     */
    private static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter(SEMANTIC);

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
        return XDF_CONVERTER.encodeNonNullable(new Int16Wrapper(type, value));
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
        return XDF_CONVERTER.decodeNonNullable(None.OBJECT, block).value;
    }
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
    /**
     * The byte length of an int16.
     */
    public static final int LENGTH = 2;
    
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
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * The XDF converter for this class.
     */
    @Immutable
    public static final class XDFConverter extends Wrapper.NonRequestingXDFConverter<Int16Wrapper> {
        
        /**
         * Creates a new XDF converter with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private XDFConverter(@Nonnull @BasedOn("int16@core.digitalid.net") SemanticType type) {
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
    public @Nonnull XDFConverter getXDFConverter() {
        return new XDFConverter(getSemanticType());
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * The SQL converter for this class.
     */
    @Immutable
    public static final class SQLConverter extends Wrapper.SQLConverter<Int16Wrapper> {
        
        /**
         * Creates a new SQL converter with the given column name.
         *
         * @param columnName the name of the database column.
         */
        private SQLConverter(@Nonnull @Validated String columnName) {
            super(Column.get(columnName, SQLType.SMALLINT), SEMANTIC);
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
    public @Nonnull SQLConverter getSQLConverter() {
        return new SQLConverter("value");
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(value);
    }
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Value Converters -------------------------------------------------- */
    
    /**
     * Returns the value XDF converter of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value XDF converter of this wrapper.
     */
    @Pure
    public static @Nonnull ValueXDFConverter<Short, Int16Wrapper> getValueXDFConverter(@Nonnull @BasedOn("int16@core.digitalid.net") SemanticType type) {
        return new ValueXDFConverter<>(FACTORY, new XDFConverter(type));
    }
    
    /**
     * Returns the value SQL converter of this wrapper.
     * 
     * @param columnName the name of the database column.
     *
     * @return the value SQL converter of this wrapper.
     */
    @Pure
    public static @Nonnull ValueSQLConverter<Short, Int16Wrapper> getValueSQLConverter(@Nonnull @Validated String columnName) {
        return new ValueSQLConverter<>(FACTORY, new SQLConverter(columnName));
    }
    
    /**
     * Returns the value converters of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * @param columnName the name of the database column.
     *
     * @return the value converters of this wrapper.
     */
    @Pure
    public static @Nonnull Converters<Short, Object> getValueConverters(@Nonnull @BasedOn("int16@core.digitalid.net") SemanticType type, @Nonnull @Validated String columnName) {
        return Converters.get(getValueXDFConverter(type), getValueSQLConverter(columnName));
    }
    
}
