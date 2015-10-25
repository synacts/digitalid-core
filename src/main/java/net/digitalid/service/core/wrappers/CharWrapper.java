package net.digitalid.service.core.wrappers;

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
 * This class wraps a {@code char} for encoding and decoding a block of the syntactic type {@code char@core.digitalid.net}.
 * <p>
 * <em>Important:</em> SQL injections have to be prevented by the caller of this class!
 * Only a warning is issued when the character might be used in an unprepared SQL statement.
 */
@Immutable
public final class CharWrapper extends Wrapper<CharWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code char@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("char@core.digitalid.net").load(0);
    
    /**
     * Stores the semantic type {@code semantic.char@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.char@core.digitalid.net").load(TYPE);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value of this wrapper.
     */
    private final char value;
    
    /**
     * Returns the value of this wrapper.
     * 
     * @return the value of this wrapper.
     */
    @Pure
    public char getValue() {
        return value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new wrapper with the given type and value.
     * 
     * @param type the semantic type of the new wrapper.
     * @param value the value of the new wrapper.
     */
    private CharWrapper(@Nonnull @Loaded @BasedOn("char@core.digitalid.net") SemanticType type, char value) {
        super(type);
        
        this.value = value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory of this class.
     */
    private static final @Nonnull Factory FACTORY = new Factory(SEMANTIC);
    
    /**
     * Encodes the given value into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * @param value the value to encode into the new block.
     * 
     * @return a new block containing the given value.
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("char@core.digitalid.net") SemanticType type, char value) {
        return FACTORY.encodeNonNullable(new CharWrapper(type, value));
    }
    
    /**
     * Decodes the given block. 
     * 
     * @param block the block to be decoded.
     * 
     * @return the value contained in the given block.
     */
    @Pure
    public static char decode(@Nonnull @NonEncoding @BasedOn("char@core.digitalid.net") Block block) throws InvalidEncodingException {
        return FACTORY.decodeNonNullable(block).value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The byte length of a char.
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static final class Factory extends Wrapper.Factory<CharWrapper> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("value", SQLType.CHAR);
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        private Factory(@Nonnull @Loaded @BasedOn("char@core.digitalid.net") SemanticType type) {
            super(type, COLUMN);
        }
        
        @Pure
        @Override
        public @Nonnull CharWrapper decodeNonNullable(@Nonnull @NonEncoding @BasedOn("char@core.digitalid.net") Block block) throws InvalidEncodingException {
            if (block.getLength() != LENGTH) throw new InvalidEncodingException("The block's length is invalid.");
            
            return new CharWrapper(block.getType(), (char) block.decodeValue());
        }
        
        @Override
        @NonCommitting
        public void setNonNullable(@Nonnull CharWrapper wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws AbortException {
            preparedStatement.setString(parameterIndex, String.valueOf(wrapper.value));
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable CharWrapper getNullable(@Nonnull ResultSet resultSet, int columnIndex) throws AbortException {
            final @Nullable String value = resultSet.getString(columnIndex);
            return value == null ? null : new CharWrapper(getType(), value.charAt(0));
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
        Log.warning("The character '" + value + "' might be used in an unprepared SQL statement and might cause an injection.", new Exception());
        return "'" + value + "'";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for the value type of this wrapper.
     */
    @Immutable
    public static class ValueFactory extends Wrapper.ValueEncodingFactory<Character, CharWrapper> {
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the type of the blocks which are returned by the factory.
         */
        private ValueFactory(@Nonnull @Loaded @BasedOn("char@core.digitalid.net") SemanticType type) {
            super(type, FACTORY);
            
            assert type.isBasedOn(TYPE) : "The given semantic type is based on the indicated syntactic type.";
        }
        
        @Pure
        @Override
        protected @Nonnull CharWrapper wrap(@Nonnull Character value) {
            return new CharWrapper(getType(), value);
        }
        
        @Pure
        @Override
        protected @Nonnull Character unwrap(@Nonnull CharWrapper wrapper) {
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
    public static @Nonnull ValueFactory getValueFactory(@Nonnull @Loaded @BasedOn("char@core.digitalid.net") SemanticType type) {
        return new ValueFactory(type);
    }
    
    /**
     * Stores the factory for the value type of this wrapper.
     */
    public static final @Nonnull ValueFactory VALUE_FACTORY = new ValueFactory(SEMANTIC);
    
}
