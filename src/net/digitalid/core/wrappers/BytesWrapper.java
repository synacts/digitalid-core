package net.digitalid.core.wrappers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.BasedOn;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Captured;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.column.Column;
import net.digitalid.core.database.Database;
import net.digitalid.core.column.SQLType;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;

/**
 * This class wraps {@code byte[]} for encoding and decoding a block of the syntactic type {@code bytes@core.digitalid.net}.
 * 
 * @invariant (bytes == null) != (block == null) : "Either the bytes or the block is null.";
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class BytesWrapper extends Wrapper<BytesWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code bytes@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("bytes@core.digitalid.net").load(0);
    
    /**
     * Stores the semantic type {@code semantic.bytes@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.bytes@core.digitalid.net").load(TYPE);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Bytes –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory of this class.
     */
    private static final @Nonnull Factory FACTORY = new Factory(SEMANTIC);
    
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
        return FACTORY.encodeNonNullable(new BytesWrapper(type, bytes));
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
        return FACTORY.decodeNonNullable(block).getBytes();
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
        return FACTORY.decodeNonNullable(block).getBytesAsInputStream();
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    protected int determineLength() {
        if (bytes != null) {
            return bytes.length + 1;
        } else {
            assert block != null : "See the class invariant.";
            return block.getLength();
        }
    }
    
    @Pure
    @Override
    protected void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        if (bytes != null) {
            block.setBytes(1, bytes);
        } else {
            assert block != null : "See the class invariant.";
            block.writeTo(block);
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static final class Factory extends Wrapper.Factory<BytesWrapper> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("bytes", SQLType.BLOB);
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        private Factory(@Nonnull @Loaded @BasedOn("bytes@core.digitalid.net") SemanticType type) {
            super(type, COLUMN);
        }
        
        @Pure
        @Override
        public @Nonnull BytesWrapper decodeNonNullable(@Nonnull @NonEncoding @BasedOn("bytes@core.digitalid.net") Block block) throws InvalidEncodingException {
            return new BytesWrapper(block);
        }
        
        @Override
        @NonCommitting
        public void setNonNullable(@Nonnull BytesWrapper wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            if (Database.getConfiguration().supportsBinaryStream()) {
                preparedStatement.setBinaryStream(parameterIndex, wrapper.getBytesAsInputStream(), wrapper.determineLength());
            } else {
                preparedStatement.setBytes(parameterIndex, wrapper.getBytes());
            }
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable BytesWrapper getNullable(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nonnull byte[] bytes = resultSet.getBytes(columnIndex);
            if (resultSet.wasNull()) return null;
            else return new BytesWrapper(getType(), bytes);
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
        if (bytes != null) {
            return Block.toString(bytes);
        } else {
            assert block != null : "See the class invariant.";
            return block.toString().replace("E'\\x00", "E'\\x");
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for the value type of this wrapper.
     */
    @Immutable
    public static class ValueFactory extends Wrapper.ValueFactory<byte[], BytesWrapper> {
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the type of the blocks which are returned by the factory.
         */
        private ValueFactory(@Nonnull @Loaded @BasedOn("bytes@core.digitalid.net") SemanticType type) {
            super(type, FACTORY);
            
            assert type.isBasedOn(TYPE) : "The given semantic type is based on the indicated syntactic type.";
        }
        
        @Pure
        @Override
        protected @Nonnull BytesWrapper wrap(@Nonnull byte[] value) {
            return new BytesWrapper(getType(), value);
        }
        
        @Pure
        @Override
        protected @Nonnull byte[] unwrap(@Nonnull BytesWrapper wrapper) {
            return wrapper.getBytes();
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
    public static @Nonnull ValueFactory getValueFactory(@Nonnull @Loaded @BasedOn("bytes@core.digitalid.net") SemanticType type) {
        return new ValueFactory(type);
    }
    
    /**
     * Stores the factory for the value type of this wrapper.
     */
    public static final @Nonnull ValueFactory VALUE_FACTORY = new ValueFactory(SEMANTIC);
    
}
