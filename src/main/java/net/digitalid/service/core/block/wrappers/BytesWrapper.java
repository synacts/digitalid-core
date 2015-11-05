package net.digitalid.service.core.block.wrappers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.Encoding;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueSQLConverter;
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueXDFConverter;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.reference.Captured;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class wraps {@code byte[]} for encoding and decoding a block of the syntactic type {@code bytes@core.digitalid.net}.
 * 
 * @invariant (bytes == null) != (block == null) : "Either the bytes or the block is null.";
 */
@Immutable
public final class BytesWrapper extends ValueWrapper<BytesWrapper> {
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Stores the syntactic type {@code bytes@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("bytes@core.digitalid.net").load(0);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
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
    private BytesWrapper(@Nonnull @Loaded @BasedOn("bytes@core.digitalid.net") SemanticType type, @Captured @Nonnull byte[] bytes) {
        super(type);
        
        this.bytes = bytes;
        this.block = null;
    }
    
    /**
     * Creates a new wrapper with the given block.
     * 
     * @param block the block of the new wrapper.
     */
    private BytesWrapper(@Nonnull @BasedOn("bytes@core.digitalid.net") Block block) {
        super(block.getType());
        
        this.bytes = null;
        this.block = block;
    }
    
    /* -------------------------------------------------- Utility -------------------------------------------------- */
    
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
        return new XDFConverter(type).encodeNonNullable(new BytesWrapper(type, bytes));
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
    public static @Capturable @Nonnull byte[] decodeNonNullable(@Nonnull @NonEncoding @BasedOn("bytes@core.digitalid.net") Block block) throws InvalidEncodingException {
        return new XDFConverter(block.getType()).decodeNonNullable(None.OBJECT, block).getBytes();
    }
    
    /**
     * Decodes the given nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the bytes contained in the given block.
     */
    @Pure
    public static @Capturable @Nullable byte[] decodeNullable(@Nullable @NonEncoding @BasedOn("bytes@core.digitalid.net") Block block) throws InvalidEncodingException {
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
    public static @Capturable @Nonnull InputStream decodeNonNullableAsInputStream(@Nonnull @NonEncoding @BasedOn("bytes@core.digitalid.net") Block block) throws InvalidEncodingException {
        return new XDFConverter(block.getType()).decodeNonNullable(None.OBJECT, block).getBytesAsInputStream();
    }
    
    /**
     * Decodes the given nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the bytes contained in the given block.
     */
    @Pure
    public static @Capturable @Nullable InputStream decodeNullableAsInputStream(@Nullable @NonEncoding @BasedOn("bytes@core.digitalid.net") Block block) throws InvalidEncodingException {
        return block == null ? null : decodeNonNullableAsInputStream(block);
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
    
    /* -------------------------------------------------- XDF -------------------------------------------------- */
    
    /**
     * The XDF converter for this class.
     */
    @Immutable
    public static final class XDFConverter extends Wrapper.NonRequestingXDFConverter<BytesWrapper> {
        
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
        public @Nonnull BytesWrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("bytes@core.digitalid.net") Block block) throws InvalidEncodingException {
            return new BytesWrapper(block);
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return new XDFConverter(getSemanticType());
    }
    
    /* -------------------------------------------------- SQL -------------------------------------------------- */
    
    /**
     * The SQL converter for this class.
     */
    @Immutable
    public static final class SQLConverter extends Wrapper.SQLConverter<BytesWrapper> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("bytes", SQLType.BLOB);
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        private SQLConverter(@Nonnull @Loaded @BasedOn("bytes@core.digitalid.net") SemanticType type) {
            super(COLUMN, type);
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull BytesWrapper wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            if (Database.getConfiguration().supportsBinaryStream()) {
                preparedStatement.setBinaryStream(parameterIndex, wrapper.getBytesAsInputStream(), wrapper.determineLength());
            } else {
                preparedStatement.setBytes(parameterIndex, wrapper.getBytes());
            }
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable BytesWrapper restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nonnull byte[] bytes = resultSet.getBytes(columnIndex);
            if (resultSet.wasNull()) return null;
            else return new BytesWrapper(getType(), bytes);
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull SQLConverter getSQLConverter() {
        return new SQLConverter(getSemanticType());
    }
    
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
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
    /**
     * The factory for this wrapper.
     */
    @Immutable
    public static class Factory extends ValueWrapper.Factory<byte[], BytesWrapper> {
        
        @Pure
        @Override
        protected @Nonnull BytesWrapper wrap(@Nonnull SemanticType type, @Nonnull byte[] value) {
            return new BytesWrapper(type, value);
        }
        
        @Pure
        @Override
        protected @Nonnull byte[] unwrap(@Nonnull BytesWrapper wrapper) {
            return wrapper.getBytes();
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
    public static @Nonnull ValueXDFConverter<byte[], BytesWrapper> getValueXDFConverter(@Nonnull @BasedOn("bytes@core.digitalid.net") SemanticType type) {
        return new ValueXDFConverter<>(FACTORY, new XDFConverter(type));
    }
    
    /**
     * Returns the value SQL converter of this wrapper.
     * 
     * @param type any semantic type that is based on the syntactic type of this wrapper.
     * 
     * @return the value SQL converter of this wrapper.
     */
    @Pure
    public static @Nonnull ValueSQLConverter<byte[], BytesWrapper> getValueSQLConverter(@Nonnull @BasedOn("bytes@core.digitalid.net") SemanticType type) {
        return new ValueSQLConverter<>(FACTORY, new SQLConverter(type));
    }
    
    /**
     * Returns the value converters of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value converters of this wrapper.
     */
    @Pure
    public static @Nonnull Converters<byte[], Object> getValueConverters(@Nonnull @BasedOn("bytes@core.digitalid.net") SemanticType type) {
        return Converters.get(getValueXDFConverter(type), getValueSQLConverter(type));
    }
    
}
