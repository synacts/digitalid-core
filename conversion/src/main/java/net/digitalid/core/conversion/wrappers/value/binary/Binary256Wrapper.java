package net.digitalid.core.conversion.wrappers.value.binary;

import java.math.BigInteger;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.index.MutableIndex;
import net.digitalid.utility.conversion.None;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Matching;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.operation.FailedValueRestoringException;
import net.digitalid.database.core.exceptions.operation.FailedValueStoringException;
import net.digitalid.database.core.exceptions.state.value.CorruptNullValueException;
import net.digitalid.database.core.sql.statement.table.create.SQLType;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.annotations.Encoding;
import net.digitalid.core.conversion.annotations.NonEncoding;
import net.digitalid.core.conversion.exceptions.InvalidBlockLengthException;
import net.digitalid.core.conversion.wrappers.AbstractWrapper;
import net.digitalid.core.conversion.wrappers.value.ValueWrapper;

import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.cryptography.Parameters;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;

/**
 * This class wraps a {@link BigInteger} for encoding and decoding a block of the syntactic type {@code hash@core.digitalid.net}.
 */
@Immutable
public final class Binary256Wrapper extends ValueWrapper<Binary256Wrapper> {
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Stores the value of this wrapper.
     */
    private final @Nonnull @NonNegative BigInteger value;
    
    /**
     * Returns the value of this wrapper.
     * 
     * @return the value of this wrapper.
     * 
     * @ensure value.bitLength() <= Parameters.HASH : "The length of the value is at most Parameters.HASH.";
     */
    @Pure
    public @Nonnull @NonNegative BigInteger getValue() {
        return value;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new wrapper with the given type and value.
     * 
     * @param type the semantic type of the new wrapper.
     * @param value the value of the new wrapper.
     * 
     * @require value.bitLength() <= Parameters.HASH : "The length of the value is at most Parameters.HASH.";
     */
    private Binary256Wrapper(@Nonnull @Loaded @BasedOn("hash@core.digitalid.net") SemanticType type, @Nonnull @NonNegative BigInteger value) {
        super(type);
        
        Require.that(value.signum() >= 0).orThrow("The value is positive.");
        Require.that(value.bitLength() <= Parameters.HASH).orThrow("The length of the value is at most Parameters.HASH.");
        
        this.value = value;
    }
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
    /**
     * The byte length of a hash.
     */
    public static final int LENGTH = 64;
    
    @Pure
    @Override
    public int determineLength() {
        return LENGTH;
    }
    
    @Pure
    @Override
    public void encode(@Nonnull @Encoding Block block) {
        Require.that(block.getLength() == determineLength()).orThrow("The block's length has to match the determined length.");
        Require.that(block.getType().isBasedOn(getSyntacticType())).orThrow("The block is based on the indicated syntactic type.");
        
        final @Nonnull byte[] bytes = value.toByteArray();
        final int offset = bytes.length > LENGTH ? 1 : 0;
        block.setBytes(LENGTH - bytes.length + offset, bytes, offset, bytes.length - offset);
    }
    
    /* -------------------------------------------------- Syntactic Type -------------------------------------------------- */
    
    /**
     * Stores the syntactic type {@code hash@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType XDF_TYPE = SyntacticType.map("hash@core.digitalid.net").load(0);
    
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
    public static final class XDFConverter extends AbstractWrapper.NonRequestingXDFConverter<Binary256Wrapper> {
        
        /**
         * Creates a new XDF converter with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private XDFConverter(@Nonnull @BasedOn("hash@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull Binary256Wrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("hash@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
            if (block.getLength() != LENGTH) { throw InvalidBlockLengthException.get(LENGTH, block.getLength()); }
            
            return new Binary256Wrapper(block.getType(), new BigInteger(1, block.getBytes()));
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return new XDFConverter(getSemanticType());
    }
    
    /* -------------------------------------------------- XDF Utility -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code semantic.int64@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.hash@core.digitalid.net").load(XDF_TYPE);
    
    /**
     * Stores a static XDF converter for performance reasons.
     */
    private static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter(SEMANTIC);
    
    /**
     * Encodes the given non-nullable value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new non-nullable block containing the given value.
     * 
     * @require value.bitLength() <= Parameters.HASH : "The length of the value is at most Parameters.HASH.";
     */
    @Pure
    public static @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull @Loaded @BasedOn("hash@core.digitalid.net") SemanticType type, @Nonnull @NonNegative BigInteger value) {
        return XDF_CONVERTER.encodeNonNullable(new Binary256Wrapper(type, value));
    }
    
    /**
     * Encodes the given nullable value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new nullable block containing the given value.
     * 
     * @require value == null || value.bitLength() <= Parameters.HASH : "The value is either null or its length is at most Parameters.HASH.";
     */
    @Pure
    public static @Nullable @NonEncoding Block encodeNullable(@Nonnull @Loaded @BasedOn("hash@core.digitalid.net") SemanticType type, @Nullable @NonNegative BigInteger value) {
        return value == null ? null : encodeNonNullable(type, value);
    }
    
    /**
     * Decodes the given non-nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     * 
     * @ensure return.bitLength() <= Parameters.HASH : "The length of the returned value is at most Parameters.HASH.";
     */
    @Pure
    public static @Nonnull @NonNegative BigInteger decodeNonNullable(@Nonnull @NonEncoding @BasedOn("hash@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return XDF_CONVERTER.decodeNonNullable(None.OBJECT, block).value;
    }
    
    /**
     * Decodes the given nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     * 
     * @ensure return == null || return.bitLength() <= Parameters.HASH : "The returned value is either null or its length is at most Parameters.HASH.";
     */
    @Pure
    public static @Nullable @NonNegative BigInteger decodeNullable(@Nullable @NonEncoding @BasedOn("hash@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return block == null ? null : decodeNonNullable(block);
    }
    
    /* -------------------------------------------------- SQL Utility -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Block.toString(value.toByteArray());
    }
    
    /**
     * Stores the given non-nullable value at the given index in the given array.
     * 
     * @param value the value which is to be stored in the values array.
     * @param values a mutable array in which the value is to be stored.
     * @param index the array index at which the value is to be stored.
     */
    public static void storeNonNullable(@Nonnull BigInteger value, @NonCaptured @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
        values.set(index.getAndIncrementValue(), Block.toString(value.toByteArray()));
    }
    
    /**
     * Stores the given nullable value at the given index in the given array.
     * 
     * @param value the value which is to be stored in the values array.
     * @param values a mutable array in which the value is to be stored.
     * @param index the array index at which the value is to be stored.
     */
    public static void storeNullable(@Nullable BigInteger value, @NonCaptured @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
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
    public static void storeNonNullable(@Nonnull BigInteger value, @NonCaptured @Nonnull ValueCollector collector) throws FailedValueStoringException {
        try {
            preparedStatement.setBytes(parameterIndex.getAndIncrementValue(), value.toByteArray());
        } catch (@Nonnull SQLException exception) {
            throw FailedValueStoringException.get(exception);
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
    public static void storeNullable(@Nullable BigInteger value, @NonCaptured @Nonnull ValueCollector collector) throws FailedValueStoringException {
        try {
            if (value != null) { storeNonNullable(value, preparedStatement, parameterIndex); }
            else { preparedStatement.setNull(parameterIndex.getAndIncrementValue(), SQL_TYPE.getCode()); }
        } catch (@Nonnull SQLException exception) {
            throw FailedValueStoringException.get(exception);
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
    public static @Nullable BigInteger restoreNullable(@NonCaptured @Nonnull SelectionResult result) throws FailedValueRestoringException {
        try {
            final @Nullable byte [] bytes = resultSet.getBytes(columnIndex.getAndIncrementValue());
            return bytes == null || bytes.length > LENGTH ? null : new BigInteger(1, bytes);
        } catch (@Nonnull SQLException exception) {
            throw FailedValueRestoringException.get(exception);
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
    public static @Nonnull BigInteger restoreNonNullable(@NonCaptured @Nonnull SelectionResult result) throws FailedValueRestoringException, CorruptNullValueException {
        final @Nullable BigInteger value = restoreNullable(resultSet, columnIndex);
        if (value == null) { throw CorruptNullValueException.get(); }
        return value;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the SQL type of this wrapper.
     */
    public static final @Nonnull SQLType SQL_TYPE = SQLType.BINARY256;
    
    /**
     * The SQL converter for this wrapper.
     */
    @Immutable
    public static final class SQLConverter extends AbstractWrapper.SQLConverter<Binary256Wrapper> {
        
        /**
         * Creates a new SQL converter with the given column declaration.
         *
         * @param declaration the declaration used to store instances of the wrapper.
         */
        private SQLConverter(@Nonnull @Matching ColumnDeclaration declaration) {
            super(declaration, SEMANTIC);
            
            Require.that(declaration.getType() == SQL_TYPE).orThrow("The declaration matches the SQL type of the wrapper.");
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull Binary256Wrapper wrapper, @NonCaptured @Nonnull ValueCollector collector) throws FailedValueStoringException {
            Binary256Wrapper.storeNonNullable(wrapper.value, preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable Binary256Wrapper restoreNullable(@Nonnull Object none, @NonCaptured @Nonnull SelectionResult result) throws FailedValueRestoringException, CorruptValueException, InternalException {
            final @Nullable BigInteger value = Binary256Wrapper.restoreNullable(resultSet, columnIndex);
            return value == null ? null : new Binary256Wrapper(getType(), value);
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
    public static class Wrapper extends ValueWrapper.Wrapper<BigInteger, Binary256Wrapper> {
        
        @Pure
        @Override
        protected boolean isValid(@Nonnull BigInteger value) {
            return value.signum() >= 0 && value.bitLength() <= Parameters.HASH;
        }
        
        @Pure
        @Override
        protected @Nonnull Binary256Wrapper wrap(@Nonnull SemanticType type, @Nonnull BigInteger value) {
            Require.that(isValid(value)).orThrow("The value is valid.");
            
            return new Binary256Wrapper(type, value);
        }
        
        @Pure
        @Override
        protected @Nonnull BigInteger unwrap(@Nonnull Binary256Wrapper wrapper) {
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
    public static @Nonnull ValueXDFConverter<BigInteger, Binary256Wrapper> getValueXDFConverter(@Nonnull @BasedOn("hash@core.digitalid.net") SemanticType type) {
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
    public static @Nonnull ValueSQLConverter<BigInteger, Binary256Wrapper> getValueSQLConverter(@Nonnull @Matching ColumnDeclaration declaration) {
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
    public static @Nonnull NonRequestingConverters<BigInteger, Object> getValueConverters(@Nonnull @BasedOn("hash@core.digitalid.net") SemanticType type, @Nonnull @Matching ColumnDeclaration declaration) {
        return NonRequestingConverters.get(getValueXDFConverter(type), getValueSQLConverter(declaration));
    }
    
}