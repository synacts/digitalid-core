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
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueEncodingFactory;
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueStoringFactory;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;

/**
 * This class wraps nothing for encoding and decoding a block of the syntactic type {@code empty@core.digitalid.net}.
 */
@Immutable
public final class EmptyWrapper extends Wrapper<EmptyWrapper> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the syntactic type {@code empty@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("empty@core.digitalid.net").load(0);
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new empty wrapper with the given type.
     * 
     * @param type the semantic type of the new wrapper.
     */
    private EmptyWrapper(@Nonnull @Loaded @BasedOn("empty@core.digitalid.net") SemanticType type) {
        super(type);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Utility –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Encodes nothing into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * 
     * @return a new block containing the given value.
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("empty@core.digitalid.net") SemanticType type) {
        return new EncodingFactory(type).encodeNonNullable(new EmptyWrapper(type));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The byte length of nothing.
     * (Blocks may not have a length of zero.)
     */
    public static final int LENGTH = 1;
    
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
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory extends Wrapper.NonRequestingEncodingFactory<EmptyWrapper> {
        
        /**
         * Creates a new encoding factory with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private EncodingFactory(@Nonnull @BasedOn("empty@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull EmptyWrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("empty@core.digitalid.net") Block block) throws InvalidEncodingException {
            if (block.getLength() != LENGTH) throw new InvalidEncodingException("The block's length is invalid.");
            
            return new EmptyWrapper(block.getType());
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull EncodingFactory getEncodingFactory() {
        return new EncodingFactory(getSemanticType());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The storing factory for this class.
     */
    @Immutable
    public static final class StoringFactory extends Wrapper.StoringFactory<EmptyWrapper> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("value", SQLType.BOOLEAN);
        
        /**
         * Creates a new storing factory with the given type.
         * 
         * @param type the semantic type of the restored wrappers.
         */
        private StoringFactory(@Nonnull @Loaded @BasedOn("empty@core.digitalid.net") SemanticType type) {
            super(COLUMN, type);
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull EmptyWrapper wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            // The entry is set to true just to indicate that it is not null. 
            preparedStatement.setBoolean(parameterIndex, true);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable EmptyWrapper restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            resultSet.getBoolean(columnIndex);
            if (resultSet.wasNull()) return null;
            else return new EmptyWrapper(getType());
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull StoringFactory getSQLConverter() {
        return new StoringFactory(getSemanticType());
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "empty";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this wrapper.
     */
    @Immutable
    public static class Factory extends ValueWrapper.Factory<Object, EmptyWrapper> {
        
        @Pure
        @Override
        protected @Nonnull EmptyWrapper wrap(@Nonnull SemanticType type, @Nonnull Object none) {
            return new EmptyWrapper(type);
        }
        
        @Pure
        @Override
        protected @Nonnull Object unwrap(@Nonnull EmptyWrapper wrapper) {
            return None.OBJECT;
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    private static final @Nonnull Factory FACTORY = new Factory();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Converters –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the value encoding factory of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value encoding factory of this wrapper.
     */
    @Pure
    public static @Nonnull ValueEncodingFactory<Object, EmptyWrapper> getValueEncodingFactory(@Nonnull @BasedOn("empty@core.digitalid.net") SemanticType type) {
        return new ValueEncodingFactory<>(FACTORY, new EncodingFactory(type));
    }
    
    /**
     * Returns the value storing factory of this wrapper.
     * 
     * @param type any semantic type that is based on the syntactic type of this wrapper.
     * 
     * @return the value storing factory of this wrapper.
     */
    @Pure
    public static @Nonnull ValueStoringFactory<Object, EmptyWrapper> getValueStoringFactory(@Nonnull @BasedOn("empty@core.digitalid.net") SemanticType type) {
        return new ValueStoringFactory<>(FACTORY, new StoringFactory(type));
    }
    
    /**
     * Returns the value factories of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value factories of this wrapper.
     */
    @Pure
    public static @Nonnull Converters<Object, Object> getValueFactories(@Nonnull @BasedOn("empty@core.digitalid.net") SemanticType type) {
        return Converters.get(getValueEncodingFactory(type), getValueStoringFactory(type));
    }
    
}
