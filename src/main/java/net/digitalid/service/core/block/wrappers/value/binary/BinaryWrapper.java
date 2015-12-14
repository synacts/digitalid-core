package net.digitalid.service.core.block.wrappers.value.binary;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.operation.FailedValueRestoringException;
import net.digitalid.database.core.exceptions.operation.FailedValueStoringException;
import net.digitalid.database.core.exceptions.state.value.CorruptNullValueException;
import net.digitalid.database.core.exceptions.state.value.CorruptValueException;
import net.digitalid.database.core.interfaces.SelectionResult;
import net.digitalid.database.core.interfaces.ValueCollector;
import net.digitalid.database.core.sql.statement.table.create.SQLType;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.Encoding;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.block.wrappers.AbstractWrapper;
import net.digitalid.service.core.block.wrappers.value.ValueWrapper;
import net.digitalid.service.core.block.wrappers.value.ValueWrapper.ValueSQLConverter;
import net.digitalid.service.core.block.wrappers.value.ValueWrapper.ValueXDFConverter;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.reference.Captured;
import net.digitalid.utility.annotations.reference.NonCapturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Matching;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.index.MutableIndex;
import net.digitalid.utility.system.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.system.exceptions.internal.InternalException;

/**
 * This class wraps {@code byte[]} for encoding and decoding a block of the syntactic type {@code bytes@core.digitalid.net}.
 * 
 * @invariant (bytes == null) != (block == null) : "Either the bytes or the block is null.";
 */
@Immutable
public final class BinaryWrapper extends ValueWrapper<BinaryWrapper> {
    
    /* -------------------------------------------------- Bytes -------------------------------------------------- */
    
    /**
     * Stores the bytes of this wrapper.
     */
    private final @Nullable byte[] bytes;
    
    /**
     * Stores the block of this wrapper.
     */
    private final @Nullable Block block;
    
    /**
     * Returns the bytes of this wrapper.
     * 
     * @return the bytes of this wrapper.
     */
    @Pure
    public @Capturable @Nonnull byte[] getBytes() {
        if (bytes != null) {
            return bytes.clone();
        } else {
            assert block != null : "See the class invariant.";
            return block.getBytes(1);
        }
    }
    
    /**
     * Returns the bytes of this wrapper as an input stream.
     * 
     * @return the bytes of this wrapper as an input stream.
     */
    @Pure
    public @Nonnull InputStream getBytesAsInputStream() {
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        } else {
            assert block != null : "See the class invariant.";
            return block.getInputStream(1);
        }
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new wrapper with the given type and bytes.
     * 
     * @param type the semantic type of the new wrapper.
     * @param bytes the bytes of the new wrapper.
     */
    private BinaryWrapper(@Nonnull @Loaded @BasedOn("bytes@core.digitalid.net") SemanticType type, @Captured @Nonnull byte[] bytes) {
        super(type);
        
        this.bytes = bytes;
        this.block = null;
    }
    
    /**
     * Creates a new wrapper with the given block.
     * 
     * @param block the block of the new wrapper.
     */
    private BinaryWrapper(@Nonnull @BasedOn("bytes@core.digitalid.net") Block block) {
        super(block.getType());
        
        this.bytes = null;
        this.block = block;
    }
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
    @Pure
    @Override
    public int determineLength() {
        if (bytes != null) {
            return bytes.length + 1;
        } else {
            assert block != null : "See the class invariant.";
            return block.getLength();
        }
    }
    
    @Pure
    @Override
    public void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        if (bytes != null) {
            block.setBytes(1, bytes);
        } else {
            assert block != null : "See the class invariant.";
            block.writeTo(block);
        }
    }
    
    /* -------------------------------------------------- Syntactic Type -------------------------------------------------- */
    
    /**
     * Stores the syntactic type {@code bytes@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType XDF_TYPE = SyntacticType.map("bytes@core.digitalid.net").load(0);
    
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
    public static final class XDFConverter extends AbstractWrapper.NonRequestingXDFConverter<BinaryWrapper> {
        
        /**
         * Creates a new XDF converter with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private XDFConverter(@Nonnull @BasedOn("bytes@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull BinaryWrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("bytes@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
            return new BinaryWrapper(block);
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return new XDFConverter(getSemanticType());
    }
    
    /* -------------------------------------------------- XDF Utility -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code semantic.bytes@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.bytes@core.digitalid.net").load(XDF_TYPE);
    
    /**
     * Stores a static XDF converter for performance reasons.
     */
    private static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter(SEMANTIC);
    
    /**
     * Encodes the given non-nullable bytes into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param bytes the bytes to encode into the new block.
     * 
     * @return a new non-nullable block containing the given bytes.
     */
    @Pure
    public static @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull @Loaded @BasedOn("bytes@core.digitalid.net") SemanticType type, @Captured @Nonnull byte[] bytes) {
        return XDF_CONVERTER.encodeNonNullable(new BinaryWrapper(type, bytes));
    }
    
    /**
     * Encodes the given nullable bytes into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param bytes the bytes to encode into the new block.
     * 
     * @return a new nullable block containing the given bytes.
     */
    @Pure
    public static @Nullable @NonEncoding Block encodeNullable(@Nonnull @Loaded @BasedOn("bytes@core.digitalid.net") SemanticType type, @Captured @Nullable byte[] bytes) {
        return bytes == null ? null : encodeNonNullable(type, bytes);
    }
    
    /**
     * Decodes the given non-nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the bytes contained in the given block.
     */
    @Pure
    public static @Capturable @Nonnull byte[] decodeNonNullable(@Nonnull @NonEncoding @BasedOn("bytes@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return XDF_CONVERTER.decodeNonNullable(None.OBJECT, block).getBytes();
    }
    
    /**
     * Decodes the given nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the bytes contained in the given block.
     */
    @Pure
    public static @Capturable @Nullable byte[] decodeNullable(@Nullable @NonEncoding @BasedOn("bytes@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return block == null ? null : decodeNonNullable(block);
    }
    
    /**
     * Decodes the given non-nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the bytes contained in the given block.
     */
    @Pure
    public static @Capturable @Nonnull InputStream decodeNonNullableAsInputStream(@Nonnull @NonEncoding @BasedOn("bytes@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return XDF_CONVERTER.decodeNonNullable(None.OBJECT, block).getBytesAsInputStream();
    }
    
    /**
     * Decodes the given nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the bytes contained in the given block.
     */
    @Pure
    public static @Capturable @Nullable InputStream decodeNullableAsInputStream(@Nullable @NonEncoding @BasedOn("bytes@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return block == null ? null : decodeNonNullableAsInputStream(block);
    }
    
    /* -------------------------------------------------- SQL Utility -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        if (bytes != null) {
            return Block.toString(bytes);
        } else {
            assert block != null : "See the class invariant.";
            return block.toString().replace("E'\\x00", "E'\\x");
        }
    }
    
    /**
     * Stores the given non-nullable bytes at the given index in the given array.
     * 
     * @param bytes the bytes which are to be stored in the values array.
     * @param values a mutable array in which the bytes are to be stored.
     * @param index the array index at which the bytes are to be stored.
     */
    public static void storeNonNullable(@Nonnull byte[] bytes, @NonCapturable @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
        values.set(index.getAndIncrementValue(), Block.toString(bytes));
    }
    
    /**
     * Stores the given nullable bytes at the given index in the given array.
     * 
     * @param bytes the bytes which are to be stored in the values array.
     * @param values a mutable array in which the bytes are to be stored.
     * @param index the array index at which the bytes are to be stored.
     */
    public static void storeNullable(@Nullable byte[] bytes, @NonCapturable @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
        if (bytes != null) { storeNonNullable(bytes, values, index); }
        else { values.set(index.getAndIncrementValue(), "NULL"); }
    }
    
    /**
     * Stores the given non-nullable bytes at the given index in the given prepared statement.
     * 
     * @param bytes the bytes which are to be stored in the given prepared statement.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the statement index at which the bytes are to be stored.
     */
    @NonCommitting
    public static void storeNonNullable(@Nonnull byte[] bytes, @NonCapturable @Nonnull ValueCollector collector) throws FailedValueStoringException {
        try {
            preparedStatement.setBytes(parameterIndex.getAndIncrementValue(), bytes);
        } catch (@Nonnull SQLException exception) {
            throw FailedValueStoringException.get(exception);
        }
    }
    
    /**
     * Stores the given nullable bytes at the given index in the given prepared statement.
     * 
     * @param bytes the bytes which are to be stored in the given prepared statement.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the statement index at which the bytes are to be stored.
     */
    @NonCommitting
    public static void storeNullable(@Nullable byte[] bytes, @NonCapturable @Nonnull ValueCollector collector) throws FailedValueStoringException {
        try {
            if (bytes != null) { storeNonNullable(bytes, preparedStatement, parameterIndex); }
            else { preparedStatement.setNull(parameterIndex.getAndIncrementValue(), SQL_TYPE.getCode()); }
        } catch (@Nonnull SQLException exception) {
            throw FailedValueStoringException.get(exception);
        }
    }
    
    /**
     * Returns the nullable bytes from the given column of the given result set.
     * 
     * @param resultSet the set from which the bytes are to be retrieved.
     * @param columnIndex the index from which the bytes are to be retrieved.
     * 
     * @return the nullable bytes from the given column of the given result set.
     */
    @Pure
    @NonCommitting
    public static @Nullable byte[] restoreNullable(@NonCapturable @Nonnull SelectionResult result) throws FailedValueRestoringException {
        try {
            return resultSet.getBytes(columnIndex.getAndIncrementValue());
        } catch (@Nonnull SQLException exception) {
            throw FailedValueRestoringException.get(exception);
        }
    }
    
    /**
     * Returns the non-nullable bytes from the given column of the given result set.
     * 
     * @param resultSet the set from which the bytes are to be retrieved.
     * @param columnIndex the index from which the bytes are to be retrieved.
     * 
     * @return the non-nullable bytes from the given column of the given result set.
     */
    @Pure
    @NonCommitting
    public static @Nonnull byte[] restoreNonNullable(@NonCapturable @Nonnull SelectionResult result) throws FailedValueRestoringException, CorruptNullValueException {
        final @Nullable byte[] bytes = restoreNullable(resultSet, columnIndex);
        if (bytes == null) { throw CorruptNullValueException.get(); }
        return bytes;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the SQL type of this wrapper.
     */
    public static final @Nonnull SQLType SQL_TYPE = SQLType.BINARY;
    
    /**
     * The SQL converter for this wrapper.
     */
    @Immutable
    public static final class SQLConverter extends AbstractWrapper.SQLConverter<BinaryWrapper> {

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
        public void storeNonNullable(@Nonnull BinaryWrapper wrapper, @NonCapturable @Nonnull ValueCollector collector) throws FailedValueStoringException {
            if (Database.getInstance().supportsBinaryStreams()) {
                collector.setBinaryStream(wrapper.getBytesAsInputStream(), wrapper.determineLength());
            } else {
                collector.setBinary(wrapper.getBytes());
            }
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable BinaryWrapper restoreNullable(@Nonnull Object none, @NonCapturable @Nonnull SelectionResult result) throws FailedValueRestoringException, CorruptValueException, InternalException {
            final @Nullable byte[] bytes = BinaryWrapper.restoreNullable(result);
            return bytes == null ? null : new BinaryWrapper(getType(), bytes);
        }
        
    }
    
    /**
     * Stores the default declaration of this wrapper.
     */
    private static final @Nonnull ColumnDeclaration DECLARATION = ColumnDeclaration.get("bytes", SQL_TYPE);
    
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
    public static class Wrapper extends ValueWrapper.Wrapper<byte[], BinaryWrapper> {
        
        @Pure
        @Override
        protected @Nonnull BinaryWrapper wrap(@Nonnull SemanticType type, @Nonnull byte[] value) {
            return new BinaryWrapper(type, value);
        }
        
        @Pure
        @Override
        protected @Nonnull byte[] unwrap(@Nonnull BinaryWrapper wrapper) {
            return wrapper.getBytes();
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
    public static @Nonnull ValueXDFConverter<byte[], BinaryWrapper> getValueXDFConverter(@Nonnull @BasedOn("bytes@core.digitalid.net") SemanticType type) {
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
    public static @Nonnull ValueSQLConverter<byte[], BinaryWrapper> getValueSQLConverter(@Nonnull @Matching ColumnDeclaration declaration) {
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
    public static @Nonnull NonRequestingConverters<byte[], Object> getValueConverters(@Nonnull @BasedOn("bytes@core.digitalid.net") SemanticType type, @Nonnull @Matching ColumnDeclaration declaration) {
        return NonRequestingConverters.get(getValueXDFConverter(type), getValueSQLConverter(declaration));
    }
    
}
