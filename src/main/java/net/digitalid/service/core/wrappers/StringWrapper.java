package net.digitalid.service.core.wrappers;

import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.BasedOn;
import net.digitalid.service.core.annotations.Encoding;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.annotations.NonEncoding;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;
import net.digitalid.utility.system.logger.Log;

/**
 * This class wraps a {@link String string} for encoding and decoding a block of the syntactic type {@code string@core.digitalid.net}.
 * <p>
 * <em>Important:</em> SQL injections have to be prevented by the caller of this class!
 * Only a warning is issued when the character might be used in an unprepared SQL statement.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class StringWrapper extends Wrapper<StringWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code string@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("string@core.digitalid.net").load(0);
    
    /**
     * Stores the semantic type {@code semantic.string@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.string@core.digitalid.net").load(TYPE);
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory of this class.
     */
    private static final @Nonnull BlockableWrapperFactory FACTORY = new BlockableWrapperFactory(SEMANTIC);
    
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
        return FACTORY.encodeNonNullable(new StringWrapper(type, value));
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
    public static @Nonnull String decodeNonNullable(@Nonnull @NonEncoding @BasedOn("string@core.digitalid.net") Block block) throws InvalidEncodingException {
        return FACTORY.decodeNonNullable(block).value;
    }
    
    /**
     * Decodes the given nullable block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     */
    @Pure
    public static @Nullable String decodeNullable(@Nullable @NonEncoding @BasedOn("string@core.digitalid.net") Block block) throws InvalidEncodingException {
        return block == null ? null : decodeNonNullable(block);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    protected int determineLength() {
        return bytes.length + 1;
    }
    
    @Pure
    @Override
    protected void encode(@Nonnull @Encoding Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        block.setBytes(1, bytes);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static final class BlockableWrapperFactory extends Wrapper.BlockableWrapperFactory<StringWrapper> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("value", SQLType.STRING);
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        private BlockableWrapperFactory(@Nonnull @Loaded @BasedOn("string@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull StringWrapper decodeNonNullable(@Nonnull @NonEncoding @BasedOn("string@core.digitalid.net") Block block) throws InvalidEncodingException {
            final @Nonnull byte[] bytes = block.getBytes(1);
            return new StringWrapper(block.getType(), bytes);
        }
    }
    
    @Pure
    @Override
    public @Nonnull Wrapper.BlockableWrapperFactory<StringWrapper> getEncodingFactory() {
        return new BlockableWrapperFactory(getSemanticType());
    }
    
    @Immutable
    public static final class StorableWrapperFactory extends Wrapper.StorableWrapperFactory<StringWrapper> {
    	
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull StringWrapper wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setString(parameterIndex, wrapper.value);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable StringWrapper restoreNullable(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable String value = resultSet.getString(columnIndex);
            return value == null ? null : new StringWrapper(getType(), value);
        }
    }
    
    public @Nonnull Wrapper.StorableWrapperFactory<StringWrapper> getStoringFactory() {
    	return new StorableWrapperFactory();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        Log.warning("The string '" + value + "' might be used in an unprepared SQL statement and might cause an injection.", new Exception());
        return "'" + value + "'";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for the value type of this wrapper.
     */
    @Immutable
    public static class ValueFactory extends Wrapper.ValueFactory<String, StringWrapper> {
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the type of the blocks which are returned by the factory.
         */
        private ValueFactory(@Nonnull @Loaded @BasedOn("string@core.digitalid.net") SemanticType type) {
            super(type, FACTORY);
            
            assert type.isBasedOn(TYPE) : "The given semantic type is based on the indicated syntactic type.";
        }
        
        @Pure
        @Override
        protected @Nonnull StringWrapper wrap(@Nonnull String value) {
            return new StringWrapper(getType(), value);
        }
        
        @Pure
        @Override
        protected @Nonnull String unwrap(@Nonnull StringWrapper wrapper) {
            return wrapper.value;
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
    public static @Nonnull ValueFactory getValueFactory(@Nonnull @Loaded @BasedOn("string@core.digitalid.net") SemanticType type) {
        return new ValueFactory(type);
    }
    
    /**
     * Stores the factory for the value type of this wrapper.
     */
    public static final @Nonnull ValueFactory VALUE_FACTORY = new ValueFactory(SEMANTIC);
    
}
