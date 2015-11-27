package net.digitalid.service.core.block.wrappers;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.Encoding;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.entity.annotations.Matching;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.reference.NonCapturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.index.MutableIndex;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.declaration.ColumnDeclaration;
import net.digitalid.utility.database.declaration.SQLType;
import net.digitalid.utility.database.exceptions.operation.FailedRestoringException;
import net.digitalid.utility.database.exceptions.operation.FailedStoringException;
import net.digitalid.utility.database.exceptions.state.CorruptNullValueException;
import net.digitalid.utility.database.exceptions.state.CorruptStateException;
import net.digitalid.utility.system.exceptions.InternalException;

/**
 * This class wraps an {@link BigInteger integer} for encoding and decoding a block of the syntactic type {@code integer@core.digitalid.net}.
 */
@Immutable
public final class IntegerWrapper extends ValueWrapper<IntegerWrapper> {
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
    @Pure
    @Override
    public int determineLength() {
        return bytes.length;
    }
    
    @Pure
    @Override
    public void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        block.setBytes(0, bytes);
    }
    
    /* -------------------------------------------------- Syntactic Type -------------------------------------------------- */
    
    /**
     * Stores the syntactic type {@code integer@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType XDF_TYPE = SyntacticType.map("integer@core.digitalid.net").load(0);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return XDF_TYPE;
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * The XDF converter for this wrapper.
     */
    @Immutable
    public static final class XDFConverter extends AbstractWrapper.NonRequestingXDFConverter<IntegerWrapper> {
        
        /**
         * Creates a new XDF converter with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private XDFConverter(@Nonnull @BasedOn("integer@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull IntegerWrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("integer@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
            final byte[] bytes = block.getBytes();
            return new IntegerWrapper(block.getType(), bytes, new BigInteger(bytes));
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return new XDFConverter(getSemanticType());
    }
    
    /* -------------------------------------------------- XDF Utility -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code semantic.integer@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.integer@core.digitalid.net").load(XDF_TYPE);
    
    /**
     * Stores a static XDF converter for performance reasons.
     */
    private static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter(SEMANTIC);
    
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
        return XDF_CONVERTER.encodeNonNullable(new IntegerWrapper(type, value.toByteArray(), value));
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
    public static @Nonnull BigInteger decodeNonNullable(@Nonnull @NonEncoding @BasedOn("integer@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return XDF_CONVERTER.decodeNonNullable(None.OBJECT, block).value;
    }
    
    /**
     * Decodes the given nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     */
    @Pure
    public static @Nullable BigInteger decodeNullable(@Nullable @NonEncoding @BasedOn("integer@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return block == null ? null : decodeNonNullable(block);
    }
    
    /* -------------------------------------------------- SQL Utility -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Block.toString(bytes);
    }
    
    /**
     * Stores the given non-nullable value at the given index in the given array.
     * 
     * @param value the value which is to be stored in the values array.
     * @param values a mutable array in which the value is to be stored.
     * @param index the array index at which the value is to be stored.
     */
    public static void storeNonNullable(@Nonnull BigInteger value, @NonCapturable @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
        values.set(index.getAndIncrementValue(), Block.toString(value.toByteArray()));
    }
    
    /**
     * Stores the given nullable value at the given index in the given array.
     * 
     * @param value the value which is to be stored in the values array.
     * @param values a mutable array in which the value is to be stored.
     * @param index the array index at which the value is to be stored.
     */
    public static void storeNullable(@Nullable BigInteger value, @NonCapturable @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
        if (value != null) { storeNonNullable(value, values, index); }
        else { values.set(index.getAndIncrementValue(), "NULL"); }
    }
    
    /**
     * Stores the given non-nullable value at the given index in the given prepared statement.
     * 
     * @param value the value which is to be stored in the given prepared statement.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the statement index at which the value is to be stored.
     */
    @NonCommitting
    public static void storeNonNullable(@Nonnull BigInteger value, @Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws FailedStoringException {
        try {
            preparedStatement.setBytes(parameterIndex.getAndIncrementValue(), value.toByteArray());
        } catch (@Nonnull SQLException exception) {
            throw FailedStoringException.get(exception);
        }
    }
    
    /**
     * Stores the given nullable value at the given index in the given prepared statement.
     * 
     * @param value the value which is to be stored in the given prepared statement.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the statement index at which the value is to be stored.
     */
    @NonCommitting
    public static void storeNullable(@Nullable BigInteger value, @Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws FailedStoringException {
        try {
            if (value != null) { storeNonNullable(value, preparedStatement, parameterIndex); }
            else { preparedStatement.setNull(parameterIndex.getAndIncrementValue(), SQL_TYPE.getCode()); }
        } catch (@Nonnull SQLException exception) {
            throw FailedStoringException.get(exception);
        }
    }
    
    /**
     * Returns the nullable value from the given column of the given result set.
     * 
     * @param resultSet the set from which the value is to be retrieved.
     * @param columnIndex the index from which the value is to be retrieved.
     * 
     * @return the nullable value from the given column of the given result set.
     */
    @Pure
    @NonCommitting
    public static @Nullable BigInteger restoreNullable(@Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws FailedRestoringException {
        try {
            final @Nullable byte [] bytes = resultSet.getBytes(columnIndex.getAndIncrementValue());
            return bytes == null ? null : new BigInteger(bytes);
        } catch (@Nonnull SQLException exception) {
            throw FailedRestoringException.get(exception);
        }
    }
    
    /**
     * Returns the non-nullable value from the given column of the given result set.
     * 
     * @param resultSet the set from which the value is to be retrieved.
     * @param columnIndex the index from which the value is to be retrieved.
     * 
     * @return the non-nullable value from the given column of the given result set.
     */
    @Pure
    @NonCommitting
    public static @Nonnull BigInteger restoreNonNullable(@Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws FailedRestoringException, CorruptNullValueException {
        final @Nullable BigInteger value = restoreNullable(resultSet, columnIndex);
        if (value == null) { throw CorruptNullValueException.get(); }
        return value;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the SQL type of this wrapper.
     */
    public static final @Nonnull SQLType SQL_TYPE = SQLType.BLOB;
    
    /**
     * The SQL converter for this wrapper.
     */
    @Immutable
    public static final class SQLConverter extends AbstractWrapper.SQLConverter<IntegerWrapper> {

        /**
         * Creates a new SQL converter with the given column declaration.
         *
         * @param declaration the declaration used to store instances of the wrapper.
         */
        private SQLConverter(@Nonnull @Matching ColumnDeclaration declaration) {
            super(declaration, SEMANTIC);
            
            assert declaration.getType() == SQL_TYPE : "The declaration matches the SQL type of the wrapper.";
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull IntegerWrapper wrapper, @Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws FailedStoringException {
            try {
                preparedStatement.setBytes(parameterIndex.getAndIncrementValue(), wrapper.bytes);
            } catch (@Nonnull SQLException exception) {
                throw FailedStoringException.get(exception);
            }
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable IntegerWrapper restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws FailedRestoringException, CorruptStateException, InternalException {
            try {
                final @Nullable byte[] bytes = resultSet.getBytes(columnIndex.getAndIncrementValue());
                return bytes == null ? null : new IntegerWrapper(getType(), bytes, new BigInteger(bytes));
            } catch (@Nonnull SQLException exception) {
                throw FailedRestoringException.get(exception);
            }
        }
        
    }
    
    /**
     * Stores the default declaration of this wrapper.
     */
    private static final @Nonnull ColumnDeclaration DECLARATION = ColumnDeclaration.get("value", SQL_TYPE);
    
    @Pure
    @Override
    public @Nonnull SQLConverter getSQLConverter() {
        return new SQLConverter(DECLARATION);
    }
    
    /* -------------------------------------------------- Wrapper -------------------------------------------------- */
    
    /**
     * The wrapper for this wrapper.
     */
    @Immutable
    public static class Wrapper extends ValueWrapper.Wrapper<BigInteger, IntegerWrapper> {
        
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
     * Stores the wrapper of this wrapper.
     */
    public static final @Nonnull Wrapper WRAPPER = new Wrapper();
    
    /* -------------------------------------------------- Value Converters -------------------------------------------------- */
    
    /**
     * Returns the value XDF converter of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value XDF converter of this wrapper.
     */
    @Pure
    public static @Nonnull ValueXDFConverter<BigInteger, IntegerWrapper> getValueXDFConverter(@Nonnull @BasedOn("integer@core.digitalid.net") SemanticType type) {
        return new ValueXDFConverter<>(WRAPPER, new XDFConverter(type));
    }
    
    /**
     * Returns the value SQL converter of this wrapper.
     * 
     * @param declaration the declaration of the converter.
     *
     * @return the value SQL converter of this wrapper.
     */
    @Pure
    public static @Nonnull ValueSQLConverter<BigInteger, IntegerWrapper> getValueSQLConverter(@Nonnull @Matching ColumnDeclaration declaration) {
        return new ValueSQLConverter<>(WRAPPER, new SQLConverter(declaration));
    }
    
    /**
     * Returns the value converters of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * @param declaration the declaration of the converter.
     *
     * @return the value converters of this wrapper.
     */
    @Pure
    public static @Nonnull NonRequestingConverters<BigInteger, Object> getValueConverters(@Nonnull @BasedOn("integer@core.digitalid.net") SemanticType type, @Nonnull @Matching ColumnDeclaration declaration) {
        return NonRequestingConverters.get(getValueXDFConverter(type), getValueSQLConverter(declaration));
    }
    
}
