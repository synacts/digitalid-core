package net.digitalid.service.core.block.wrappers;

import net.digitalid.service.core.block.annotations.NonEncoding;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.identity.annotations.Loaded;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.storing.AbstractStoringFactory;

/**
 * A value wrapper wraps a primitive value.
 */
@Immutable
public abstract class ValueWrapper<W extends ValueWrapper<W>> extends Wrapper<W> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public abstract @Nonnull Wrapper.NonRequestingEncodingFactory<W> getEncodingFactory();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for wrappers.
     */
    @Immutable
    public abstract static class Factory<V, W extends Wrapper<W>> {
        
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
         * 
         * @require isValid(value) : "The value is valid.";
         */
        @Pure
        protected abstract @Nonnull W wrap(@Nonnull SemanticType type, @Nonnull V value);
        
        /**
         * Unwraps the given wrapper.
         * 
         * @param wrapper the wrapper to unwrap.
         * 
         * @return the value wrapped in the given wrapper.
         * 
         * @ensure isValid(value) : "The value is valid.";
         */
        @Pure
        protected abstract @Nonnull V unwrap(@Nonnull W wrapper);
        
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Encoding Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for encoding and decoding values.
     */
    @Immutable
    public final static class ValueEncodingFactory<V, W extends Wrapper<W>> extends net.digitalid.service.core.factory.encoding.NonRequestingEncodingFactory<V, Object> {
        
        /**
         * Stores the factory to wrap and unwrap the values.
         */
        private final @Nonnull Factory<V, W> factory;
        
        /**
         * Stores the encoding factory to encode and decode the wrapped values.
         */
        private final @Nonnull NonRequestingEncodingFactory<W> encodingFactory;
        
        /**
         * Creates a new value encoding factory with the given parameters.
         * 
         * @param factory the factory that allows to wrap and unwrap the values.
         * @param encodingFactory the encoding factory that allows to encode and decode the wrapped values.
         */
        protected ValueEncodingFactory(@Nonnull Factory<V, W> factory, @Nonnull NonRequestingEncodingFactory<W> encodingFactory) {
            super(encodingFactory.getType());
            
            this.factory = factory;
            this.encodingFactory = encodingFactory;
        }
        
        @Pure
        @Override
        public final @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull V value) {
            return encodingFactory.encodeNonNullable(factory.wrap(encodingFactory.getType(), value));
        }
        
        @Pure
        @Locked
        @Override
        @NonCommitting
        public final @Nonnull V decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the type of this factory.";
            
            return factory.unwrap(encodingFactory.decodeNonNullable(none, block));
        }
        
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value Storing Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for storing and restoring values.
     */
    @Immutable
    public final static class ValueStoringFactory<V, W extends Wrapper<W>> extends AbstractStoringFactory<V, Object> {
        
        /**
         * Stores the factory to wrap and unwrap the values.
         */
        private final @Nonnull Factory<V, W> factory;
        
        /**
         * Stores the storing factory to store and restore the wrapped values.
         */
        private final @Nonnull StoringFactory<W> storingFactory;
        
        /**
         * Creates a new storing factory with the given parameters.
         * 
         * @param factory the factory that allows to wrap and unwrap the values.
         * @param storingFactory the storing factory that allows to store and restore the wrapped values.
         */
        protected ValueStoringFactory(@Nonnull Factory<V, W> factory, @Nonnull StoringFactory<W> storingFactory) {
            super(storingFactory.getColumns());
            
            this.factory = factory;
            this.storingFactory = storingFactory;
        }
        
        @Pure
        @Override
        public final @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull V value) {
            return FreezableArray.getNonNullable(factory.wrap(storingFactory.getType(), value).toString());
        }
        
        @Override
        @NonCommitting
        public final void storeNonNullable(@Nonnull V value, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            storingFactory.storeNonNullable(factory.wrap(storingFactory.getType(), value), preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public final @Nullable V restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable W wrapper = storingFactory.restoreNullable(none, resultSet, columnIndex);
            return wrapper == null ? null : factory.unwrap(wrapper);
        }
        
    }
    
}
