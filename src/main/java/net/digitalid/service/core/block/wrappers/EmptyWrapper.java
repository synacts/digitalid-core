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
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueSQLConverter;
import net.digitalid.service.core.block.wrappers.ValueWrapper.ValueXDFConverter;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.declaration.SQLType;

/**
 * This class wraps nothing for encoding and decoding a block of the syntactic type {@code empty@core.digitalid.net}.
 */
@Immutable
public final class EmptyWrapper extends ValueWrapper<EmptyWrapper> {
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Stores the syntactic type {@code empty@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.map("empty@core.digitalid.net").load(0);

    /**
     * Stores the syntactic type {@code semantic.int64@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SEMANTIC = SemanticType.map("semantic.int64@core.digitalid.net").load(TYPE);

    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new empty wrapper with the given type.
     * 
     * @param type the semantic type of the new wrapper.
     */
    private EmptyWrapper(@Nonnull @Loaded @BasedOn("empty@core.digitalid.net") SemanticType type) {
        super(type);
    }
    
    /* -------------------------------------------------- Utility -------------------------------------------------- */

    /**
     * Stores a static XDF converter for performance reasons.
     */
    private static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter(SEMANTIC);

    /**
     * Encodes nothing into a new block of the given type.
     * 
     * @param type the semantic type of the new block.
     * 
     * @return a new block containing the given value.
     */
    @Pure
    public static @Nonnull @NonEncoding Block encode(@Nonnull @Loaded @BasedOn("empty@core.digitalid.net") SemanticType type) {
        return XDF_CONVERTER.encodeNonNullable(new EmptyWrapper(type));
    }
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * The XDF converter for this class.
     */
    @Immutable
    public static final class XDFConverter extends AbstractWrapper.NonRequestingXDFConverter<EmptyWrapper> {
        
        /**
         * Creates a new XDF converter with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        private XDFConverter(@Nonnull @BasedOn("empty@core.digitalid.net") SemanticType type) {
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
    public @Nonnull XDFConverter getXDFConverter() {
        return new XDFConverter(getSemanticType());
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * The SQL converter for this class.
     */
    @Immutable
    public static final class SQLConverter extends AbstractWrapper.SQLConverter<EmptyWrapper> {
        
        /**
         * Creates a new SQL converter with the given column name.
         *
         * @param columnName the name of the database column.
         */
        private SQLConverter(@Nonnull @Validated String columnName) {
            super(Column.get(columnName, SQLType.BOOLEAN), SEMANTIC);
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
    public @Nonnull SQLConverter getSQLConverter() {
        return new SQLConverter("value");
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "empty";
    }
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
    /**
     * The factory for this wrapper.
     */
    @Immutable
    public static class Factory extends ValueWrapper.Wrapper<Object, EmptyWrapper> {
        
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
    
    /* -------------------------------------------------- Value Converters -------------------------------------------------- */
    
    /**
     * Returns the value XDF converter of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * 
     * @return the value XDF converter of this wrapper.
     */
    @Pure
    public static @Nonnull ValueXDFConverter<Object, EmptyWrapper> getValueXDFConverter(@Nonnull @BasedOn("empty@core.digitalid.net") SemanticType type) {
        return new ValueXDFConverter<>(FACTORY, new XDFConverter(type));
    }
    
    /**
     * Returns the value SQL converter of this wrapper.
     * 
     * @param columnName the name of the database column.
     *
     * @return the value SQL converter of this wrapper.
     */
    @Pure
    public static @Nonnull ValueSQLConverter<Object, EmptyWrapper> getValueSQLConverter(@Nonnull @Validated String columnName) {
        return new ValueSQLConverter<>(FACTORY, new SQLConverter(columnName));
    }
    
    /**
     * Returns the value converters of this wrapper.
     * 
     * @param type the semantic type of the encoded blocks.
     * @param columnName the name of the database column.
     *
     * @return the value converters of this wrapper.
     */
    @Pure
    public static @Nonnull Converters<Object, Object> getValueConverters(@Nonnull @BasedOn("empty@core.digitalid.net") SemanticType type, @Nonnull @Validated String columnName) {
        return Converters.get(getValueXDFConverter(type), getValueSQLConverter(columnName));
    }
    
}
