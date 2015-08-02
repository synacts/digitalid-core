package net.digitalid.core.wrappers;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.BasedOn;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.database.Column;
import net.digitalid.core.database.SQLType;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.SyntacticType;

/**
 * Wraps values of the syntactic type {@code integer@core.digitalid.net} for encoding and decoding.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class IntegerWrapper extends Wrapper<IntegerWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code integer@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("integer@core.digitalid.net").load(0);
    
    /**
     * Stores the semantic type {@code semantic.integer@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.integer@core.digitalid.net").load(TYPE);
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory of this class.
     */
    private static final Wrapper.Factory<IntegerWrapper> FACTORY = new Factory(SEMANTIC);
    
    /**
     * Encodes the given value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new block containing the given value.
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("integer@core.digitalid.net") SemanticType type, @Nonnull BigInteger value) {
        return FACTORY.encodeNonNullable(new IntegerWrapper(type, value.toByteArray(), value));
    }
    
    /**
     * Decodes the given block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     */
    @Pure
    public static @Nonnull BigInteger decode(@Nonnull @NonEncoding @BasedOn("integer@core.digitalid.net") Block block) throws InvalidEncodingException {
        return FACTORY.decodeNonNullable(block).value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    protected int determineLength() {
        return bytes.length;
    }
    
    @Pure
    @Override
    protected void encode(@Encoding @Nonnull Block block) {
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        
        block.setBytes(0, bytes);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    private static class Factory extends Wrapper.Factory<IntegerWrapper> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("value", SQLType.BLOB);
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type that corresponds to the wrapper.
         */
        protected Factory(@Nonnull @Loaded @BasedOn("integer@core.digitalid.net") SemanticType type) {
            super(type, COLUMN);
            
            assert type.isBasedOn(TYPE) : "The given semantic type is based on the indicated syntactic type.";
        }
        
        @Pure
        @Override
        public @Nonnull IntegerWrapper decodeNonNullable(@Nonnull @NonEncoding Block block) throws InvalidEncodingException {
            final byte[] bytes = block.getBytes();
            return new IntegerWrapper(block.getType(), bytes, new BigInteger(bytes));
        }
        
        @Override
        public void setNonNullable(@Nonnull IntegerWrapper wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setBytes(parameterIndex, wrapper.bytes);
        }
        
        @Pure
        @Override
        public @Nullable IntegerWrapper getNullable(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable byte[] bytes = resultSet.getBytes(columnIndex);
            return bytes == null ? null : new IntegerWrapper(getType(), bytes, new BigInteger(bytes));
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull Wrapper.Factory<IntegerWrapper> getFactory() {
        return new Factory(getSemanticType());
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("E'\\x");
        for (int i = 0; i < bytes.length; i++) {
            string.append(String.format("%02X", bytes[i]));
        }
        string.append("'");
        return string.toString();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for the value type of this wrapper.
     */
    public static class ValueFactory extends Wrapper.ValueFactory<BigInteger, IntegerWrapper> {
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type that corresponds to the wrapper.
         */
        protected ValueFactory(@Nonnull @Loaded @BasedOn("integer@core.digitalid.net") SemanticType type) {
            super(type, FACTORY);
            
            assert type.isBasedOn(TYPE) : "The given semantic type is based on the indicated syntactic type.";
        }
        
        @Pure
        @Override
        protected @Nonnull IntegerWrapper wrap(@Nonnull BigInteger value) {
            return new IntegerWrapper(getType(), value.toByteArray(), value);
        }
        
        @Pure
        @Override
        protected @Nonnull BigInteger unwrap(@Nonnull IntegerWrapper wrapper) {
            return wrapper.value;
        }
        
    }
    
    /**
     * Stores the factory for the value type of this wrapper.
     */
    public static final @Nonnull ValueFactory VALUE_FACTORY = new ValueFactory(SEMANTIC);
    
}
