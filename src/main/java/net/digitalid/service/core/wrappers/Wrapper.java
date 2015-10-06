package net.digitalid.service.core.wrappers;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.Encoding;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.annotations.NonEncoding;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.factory.GlobalFactory;
import net.digitalid.service.core.factory.Storable;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.utility.annotations.math.Positive;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;

/**
 * Values and elements are wrapped by separate objects as the native types do not support encoding and decoding.
 * 
 * @see Block
 *
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class Wrapper<W extends Wrapper<W>> implements Storable<W, Object> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type of this wrapper.
     * 
     * @invariant semanticType.isBasedOn(getSyntacticType()) : "The semantic type is based on the syntactic type.";
     */
    private final @Nonnull SemanticType semanticType;
    
    /**
     * Returns the semantic type of this wrapper.
     * 
     * @return the semantic type of this wrapper.
     * 
     * @ensure semanticType.isBasedOn(getSyntacticType()) : "The semantic type is based on the syntactic type.";
     */
    @Pure
    public final @Nonnull SemanticType getSemanticType() {
        return semanticType;
    }
    
    /**
     * Returns the syntactic type that corresponds to this class.
     * 
     * @return the syntactic type that corresponds to this class.
     */
    @Pure
    public abstract @Nonnull @Loaded SyntacticType getSyntacticType();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new wrapper with the given semantic type.
     * 
     * @param semanticType the semantic type of the new wrapper.
     * 
     * @require semanticType.isBasedOn(getSyntacticType()) : "The given semantic type is based on the indicated syntactic type.";
     */
    protected Wrapper(@Nonnull @Loaded SemanticType semanticType) {
        assert semanticType.isBasedOn(getSyntacticType()) : "The given semantic type is based on the indicated syntactic type.";
        
        this.semanticType = semanticType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Determines the length of the encoding block.
     * This method is required for lazy encoding.
     * 
     * @return the length of the encoding block.
     */
    @Pure
    protected abstract @Positive int determineLength();
    
    /**
     * Encodes the data into the encoding block.
     * This method is required for lazy encoding.
     * <p>
     * <em>Important:</em> Do not leak the given block!
     * 
     * @param block an encoding block to encode the data into.
     * 
     * @require block.getLength() == determineLength() : "The block's length has to match the determined length.";
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    protected abstract void encode(@Encoding @Nonnull Block block);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Wrapper)) return false;
        final @Nonnull Wrapper<?> other = (Wrapper) object;
        return this.getClass().equals(other.getClass()) && Block.fromNonNullable((W) this).equals(Block.fromNonNullable((W) other));
    }
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public final int hashCode() {
        return Block.fromNonNullable((W) this).hashCode();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for wrappers.
     */
    @Immutable
    public abstract static class Factory<W extends Wrapper<W>> extends GlobalFactory<W, Object> {
        
        /**
         * Creates a new factory with the given parameters.
         * 
         * @param type the semantic type that corresponds to the wrapper.
         * @param column the column used to store objects of the wrapper.
         */
        protected Factory(@Nonnull @Loaded SemanticType type, @Nonnull @NonNullableElements Column column) {
            super(type, column);
        }
        
        @Pure
        @Override
        public final @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull W wrapper) {
            // This implementation violates the postcondition for static wrapper factories.
            return Block.get(wrapper.getSemanticType(), wrapper);
        }
        
        @Pure
        @Override
        public final @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull W wrapper) {
            return FreezableArray.getNonNullable(wrapper.toString());
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull Factory<W> getFactory();
    
    /**
     * Returns the value of this wrapper for SQL.
     * 
     * @return the value of this wrapper for SQL.
     */
    @Pure
    @Override
    public abstract @Nonnull String toString();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for values.
     */
    @Immutable
    public abstract static class ValueFactory<V, W extends Wrapper<W>> extends GlobalFactory<V, Object> {
        
        /**
         * Stores the factory to wrap and unwrap the values.
         */
        private @Nonnull Factory<W> factory;
        
        /**
         * Creates a new factory with the given type and factory.
         * 
         * @param type the semantic type that of the surrounding wrapper.
         * @param factory the factory that allows to wrap and unwrap the values.
         */
        protected ValueFactory(@Nonnull @Loaded SemanticType type, @Nonnull Factory<W> factory) {
            super(type, factory.getColumns().getNonNullable(0));
        }
        
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
         * @param value the value to wrap.
         * 
         * @return the wrapper around the value.
         * 
         * @require isValid(value) : "The value is valid.";
         */
        @Pure
        protected abstract @Nonnull W wrap(@Nonnull V value);
        
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
        
        @Pure
        @Override
        public final @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull V value) {
            return factory.encodeNonNullable(wrap(value));
        }
        
        @Pure
        @Locked
        @Override
        @NonCommitting
        public final @Nonnull V decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding Block block) throws SQLException, IOException, PacketException, ExternalException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the type of this factory.";
            
            return unwrap(factory.decodeNonNullable(none, block));
        }
        
        @Pure
        @Override
        public final @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull V value) {
            return FreezableArray.getNonNullable(wrap(value).toString());
        }
        
        @Override
        @NonCommitting
        public final void setNonNullable(@Nonnull V value, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            factory.setNonNullable(wrap(value), preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public final @Nullable V getNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable W wrapper = factory.getNullable(none, resultSet, columnIndex);
            return wrapper == null ? null : unwrap(wrapper);
        }
        
    }
    
}
