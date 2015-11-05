package net.digitalid.service.core.block.wrappers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

/**
 * A value wrapper wraps a primitive value.
 */
@Immutable
public abstract class ValueWrapper<W extends ValueWrapper<W>> extends Wrapper<W> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new value wrapper with the given semantic type.
     * 
     * @param semanticType the semantic type of the new wrapper.
     * 
     * @require semanticType.isBasedOn(getSyntacticType()) : "The given semantic type is based on the indicated syntactic type.";
     */
    protected ValueWrapper(@Nonnull @Loaded SemanticType semanticType) {
        super(semanticType);
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull Wrapper.NonRequestingXDFConverter<W> getXDFConverter();
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
    /**
     * The factory for wrappers.
     */
    @Immutable
    public abstract static class Factory<V, W extends ValueWrapper<W>> {
        
        /**
         * Returns whether the given value is valid.
         * 
         * @param value the value to check.
         * 
         * @return whether the given value is valid.
         */
        @Pure
        protected boolean isValid(@Nonnull V value) {
            return true;
        }
        
        /**
         * Wraps the given value.
         * 
         * @param type the type of the wrapper.
         * @param value the value to wrap.
         * 
         * @return the wrapper around the value.
         */
        @Pure
        protected abstract @Nonnull W wrap(@Nonnull SemanticType type, @Nonnull @Validated V value);
        
        /**
         * Unwraps the given wrapper.
         * 
         * @param wrapper the wrapper to unwrap.
         * 
         * @return the value wrapped in the given wrapper.
         */
        @Pure
        protected abstract @Nonnull @Validated V unwrap(@Nonnull W wrapper);
        
    }
    
    /* -------------------------------------------------- Value XDF Converter -------------------------------------------------- */
    
    /**
     * The factory for encoding and decoding values.
     */
    @Immutable
    public final static class ValueXDFConverter<V, W extends ValueWrapper<W>> extends AbstractNonRequestingXDFConverter<V, Object> {
        
        /**
         * Stores the factory to wrap and unwrap the values.
         */
        private final @Nonnull Factory<V, W> factory;
        
        /**
         * Stores the XDF converter to encode and decode the wrapped values.
         */
        private final @Nonnull NonRequestingXDFConverter<W> XDFConverter;
        
        /**
         * Creates a new value XDF converter with the given parameters.
         * 
         * @param factory the factory that allows to wrap and unwrap the values.
         * @param XDFConverter the XDF converter that allows to encode and decode the wrapped values.
         */
        protected ValueXDFConverter(@Nonnull Factory<V, W> factory, @Nonnull NonRequestingXDFConverter<W> XDFConverter) {
            super(XDFConverter.getType());
            
            this.factory = factory;
            this.XDFConverter = XDFConverter;
        }
        
        @Pure
        @Override
        public final @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull V value) {
            return XDFConverter.encodeNonNullable(factory.wrap(XDFConverter.getType(), value));
        }
        
        @Pure
        @Locked
        @Override
        @NonCommitting
        public final @Nonnull V decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the type of this converter.";
            
            return factory.unwrap(XDFConverter.decodeNonNullable(none, block));
        }
        
    }
    
    /* -------------------------------------------------- Value SQL Converter -------------------------------------------------- */
    
    /**
     * The factory for storing and restoring values.
     */
    @Immutable
    public final static class ValueSQLConverter<V, W extends ValueWrapper<W>> extends AbstractSQLConverter<V, Object> {
        
        /**
         * Stores the factory to wrap and unwrap the values.
         */
        private final @Nonnull Factory<V, W> factory;
        
        /**
         * Stores the SQL converter to store and restore the wrapped values.
         */
        private final @Nonnull SQLConverter<W> SQLConverter;
        
        /**
         * Creates a new SQL converter with the given parameters.
         * 
         * @param factory the factory that allows to wrap and unwrap the values.
         * @param SQLConverter the SQL converter that allows to store and restore the wrapped values.
         */
        protected ValueSQLConverter(@Nonnull Factory<V, W> factory, @Nonnull SQLConverter<W> SQLConverter) {
            super(SQLConverter.getColumns());
            
            this.factory = factory;
            this.SQLConverter = SQLConverter;
        }
        
        @Pure
        @Override
        public final @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull V value) {
            return FreezableArray.getNonNullable(factory.wrap(SQLConverter.getType(), value).toString());
        }
        
        @Override
        @NonCommitting
        public final void storeNonNullable(@Nonnull V value, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            SQLConverter.storeNonNullable(factory.wrap(SQLConverter.getType(), value), preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public final @Nullable V restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable W wrapper = SQLConverter.restoreNullable(none, resultSet, columnIndex);
            return wrapper == null ? null : factory.unwrap(wrapper);
        }
        
    }
    
}
