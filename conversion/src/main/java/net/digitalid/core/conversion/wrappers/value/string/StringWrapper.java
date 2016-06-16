package net.digitalid.core.conversion.wrappers.value.string;

import java.nio.charset.Charset;
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
import net.digitalid.utility.system.logger.Log;
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
import net.digitalid.core.conversion.wrappers.AbstractWrapper;
import net.digitalid.core.conversion.wrappers.value.ValueWrapper;

import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;

/**
 * This class wraps a {@link String string} for encoding and decoding a block of the syntactic type {@code string@core.digitalid.net}.
 * <p>
 * <em>Important:</em> SQL injections have to be prevented by the caller of this wrapper!
 * Only a warning is issued when the character might be used in an unprepared SQL statement.
 */
@Immutable
public final class StringWrapper extends ValueWrapper<StringWrapper> {
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Stores the value as a byte array.
     */
    private final @Nonnull byte[] bytes;
    
    /**
     * Stores the value of this wrapper.
     */
    private final @Nonnull String value;
    
    /**
     * Returns the value of this wrapper.
     * 
     * @return the value of this wrapper.
     */
    @Pure
    public @Nonnull String getValue() {
        return value;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Stores the character set used to encode and decode strings.
     */
    public static final @Nonnull Charset CHARSET = Charset.forName("UTF-16BE");
    
    /**
     * Creates a new wrapper with the given type and bytes.
     * 
     * @param type the semantic type of the new wrapper.
     * @param bytes the value as a byte array.
     */
    private StringWrapper(@Nonnull @Loaded @BasedOn("string@core.digitalid.net") SemanticType type, @Nonnull byte[] bytes) {
        super(type);
        
        this.bytes = bytes;
        this.value = new String(bytes, 0, bytes.length, CHARSET);
    }
    
    /**
     * Creates a new wrapper with the given type and value.
     * 
     * @param type the semantic type of the new wrapper.
     * @param value the value of the new wrapper.
     */
    private StringWrapper(@Nonnull @Loaded @BasedOn("string@core.digitalid.net") SemanticType type, @Nonnull String value) {
        super(type);
        
        this.bytes = value.getBytes(CHARSET);
        this.value = value;
    }
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
    @Pure
    @Override
    public int determineLength() {
        return bytes.length + 1;
    }
    
    @Pure
    @Override
    public void encode(@Nonnull @Encoding Block block) {
        Require.that(block.getLength() == determineLength()).orThrow("The block's length has to match the determined length.");
        Require.that(block.getType().isBasedOn(getSyntacticType())).orThrow("The block is based on the indicated syntactic type.");
        
        block.setBytes(1, bytes);
    }
    
    /* -------------------------------------------------- Syntactic Type -------------------------------------------------- */
    
    /**
     * Stores the syntactic type {@code string@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType XDF_TYPE = SyntacticType.map("string@core.digitalid.net").load(0);
    
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
    public static final class XDFConverter extends AbstractWrapper.NonRequestingXDFConverter<StringWrapper> {
        
        /**
         * Creates a new XDF converter with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private XDFConverter(@Nonnull @Loaded @BasedOn("string@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull StringWrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("string@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
            final @Nonnull byte[] bytes = block.getBytes(1);
            return new StringWrapper(block.getType(), bytes);
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return new XDFConverter(getSemanticType());
    }
    
    /* -------------------------------------------------- XDF Utility -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code semantic.string@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.string@core.digitalid.net").load(XDF_TYPE);
    
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
     */
    @Pure
    public static @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull @Loaded @BasedOn("string@core.digitalid.net") SemanticType type, @Nonnull String value) {
        return XDF_CONVERTER.encodeNonNullable(new StringWrapper(type, value));
    }
    
    /**
     * Encodes the given nullable value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new nullable block containing the given value.
     */
    @Pure
    public static @Nullable @NonEncoding Block encodeNullable(@Nonnull @Loaded @BasedOn("string@core.digitalid.net") SemanticType type, @Nullable String value) {
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
    public static @Nonnull String decodeNonNullable(@Nonnull @NonEncoding @BasedOn("string@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
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
    public static @Nullable String decodeNullable(@Nullable @NonEncoding @BasedOn("string@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return block == null ? null : decodeNonNullable(block);
    }
    
    /* -------------------------------------------------- SQL Utility -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        Log.warning("The string '" + value + "' might be used in an unprepared SQL statement and might cause an injection.", new Exception());
        return "'" + value + "'";
    }
    
    /**
     * Stores the given non-nullable value at the given index in the given array.
     * 
     * @param value the value which is to be stored in the values array.
     * @param values a mutable array in which the value is to be stored.
     * @param index the array index at which the value is to be stored.
     */
    public static void storeNonNullable(@Nonnull String value, @NonCaptured @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
        Log.warning("The string '" + value + "' might be used in an unprepared SQL statement and might cause an injection.", new Exception());
        values.set(index.getAndIncrementValue(), "'" + value + "'");
    }
    
    /**
     * Stores the given nullable value at the given index in the given array.
     * 
     * @param value the value which is to be stored in the values array.
     * @param values a mutable array in which the value is to be stored.
     * @param index the array index at which the value is to be stored.
     */
    public static void storeNullable(@Nullable String value, @NonCaptured @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
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
    public static void storeNonNullable(@Nonnull String value, @NonCaptured @Nonnull ValueCollector collector) throws FailedValueStoringException {
        try {
            preparedStatement.setString(parameterIndex.getAndIncrementValue(), value);
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
    public static void storeNullable(@Nullable String value, @NonCaptured @Nonnull ValueCollector collector) throws FailedValueStoringException {
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
    public static @Nullable String restoreNullable(@NonCaptured @Nonnull SelectionResult result) throws FailedValueRestoringException {
        try {
            return resultSet.getString(columnIndex.getAndIncrementValue());
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
    public static @Nonnull String restoreNonNullable(@NonCaptured @Nonnull SelectionResult result) throws FailedValueRestoringException, CorruptNullValueException {
        final @Nullable String value = restoreNullable(resultSet, columnIndex);
        if (value == null) { throw CorruptNullValueException.get(); }
        return value;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the SQL type of this wrapper.
     */
    public static final @Nonnull SQLType SQL_TYPE = SQLType.STRING;
    
    /**
     * The SQL converter for this wrapper.
     */
    @Immutable
    public static final class SQLConverter extends AbstractWrapper.SQLConverter<StringWrapper> {
        
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
        public void storeNonNullable(@Nonnull StringWrapper wrapper, @NonCaptured @Nonnull ValueCollector collector) throws FailedValueStoringException {
            StringWrapper.storeNonNullable(wrapper.value, preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable StringWrapper restoreNullable(@Nonnull Object none, @NonCaptured @Nonnull SelectionResult result) throws FailedValueRestoringException, CorruptValueException, InternalException {
            final @Nullable String value = StringWrapper.restoreNullable(resultSet, columnIndex);
            return value == null ? null : new StringWrapper(getType(), value);
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
    public static class Wrapper extends ValueWrapper.Wrapper<String, StringWrapper> {
        
        @Pure
        @Override
        protected @Nonnull StringWrapper wrap(@Nonnull SemanticType type, @Nonnull String value) {
            return new StringWrapper(type, value);
        }
        
        @Pure
        @Override
        protected @Nonnull String unwrap(@Nonnull StringWrapper wrapper) {
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
    public static @Nonnull ValueXDFConverter<String, StringWrapper> getValueXDFConverter(@Nonnull @BasedOn("string@core.digitalid.net") SemanticType type) {
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
    public static @Nonnull ValueSQLConverter<String, StringWrapper> getValueSQLConverter(@Nonnull @Matching ColumnDeclaration declaration) {
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
    public static @Nonnull NonRequestingConverters<String, Object> getValueConverters(@Nonnull @BasedOn("string@core.digitalid.net") SemanticType type, @Nonnull @Matching ColumnDeclaration declaration) {
        return NonRequestingConverters.get(getValueXDFConverter(type), getValueSQLConverter(declaration));
    }
    
}