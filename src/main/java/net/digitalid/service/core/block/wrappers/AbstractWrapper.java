package net.digitalid.service.core.block.wrappers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.Encoding;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ConvertToXDF;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.exceptions.internal.InternalException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.math.Positive;
import net.digitalid.utility.annotations.reference.NonCapturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.index.MutableIndex;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.converter.SQL;
import net.digitalid.utility.database.declaration.Declaration;

/**
 * Values and elements are wrapped by separate objects as the native types do not support encoding and decoding.
 * 
 * @see Block
 */
@Immutable
public abstract class AbstractWrapper<W extends AbstractWrapper<W>> implements XDF<W, Object>, SQL<W, Object> {
    
    /* -------------------------------------------------- Semantic Type -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Syntactic Type -------------------------------------------------- */
    
    /**
     * Returns the syntactic type that corresponds to this class.
     * 
     * @return the syntactic type that corresponds to this class.
     */
    @Pure
    public abstract @Nonnull @Loaded SyntacticType getSyntacticType();
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new wrapper with the given semantic type.
     * 
     * @param semanticType the semantic type of the new wrapper.
     * 
     * @require semanticType.isBasedOn(getSyntacticType()) : "The given semantic type is based on the indicated syntactic type.";
     */
    protected AbstractWrapper(@Nonnull @Loaded SemanticType semanticType) {
        assert semanticType.isBasedOn(getSyntacticType()) : "The given semantic type is based on the indicated syntactic type.";
        
        this.semanticType = semanticType;
    }
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
    /**
     * Determines the length of the encoding block.
     * This method is required for lazy encoding.
     * 
     * @return the length of the encoding block.
     */
    @Pure
    public abstract @Positive int determineLength();
    
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
    public abstract void encode(@Encoding @Nonnull Block block);
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public final boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof AbstractWrapper)) { return false; }
        final @Nonnull AbstractWrapper<?> other = (AbstractWrapper<?>) object;
        return this.getClass().equals(other.getClass()) && ConvertToXDF.nonNullable((W) this).equals(ConvertToXDF.nonNullable((W) other));
    }
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public final int hashCode() {
        return ConvertToXDF.nonNullable((W) this).hashCode();
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * The XDF converter for wrappers.
     */
    @Immutable
    public abstract static class XDFConverter<W extends AbstractWrapper<W>> extends AbstractXDFConverter<W, Object> {
        
        /**
         * Creates a new XDF converter with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        protected XDFConverter(@Nonnull @Loaded SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public final @Nonnull @NonEncoding Block encodeNonNullable(@Nonnull W wrapper) {
            // The following implementation violates the postcondition of this method.
            // The correct implementation would be 'Block.get(getType(), wrapper)'.
            // However, we use static XDF converters for performance reasons.
            return Block.get(wrapper.getSemanticType(), wrapper);
        }
        
    }
    
    /**
     * The non-requesting XDF converter for wrappers.
     */
    @Immutable
    public abstract static class NonRequestingXDFConverter<W extends AbstractWrapper<W>> extends XDFConverter<W> {
        
        /**
         * Creates a new non-requesting XDF converter with the given type.
         * 
         * @param type the semantic type of the encoded blocks and decoded wrappers.
         */
        protected NonRequestingXDFConverter(@Nonnull @Loaded SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public abstract @Nonnull W decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding Block block) throws InvalidEncodingException, InternalException;
        
        @Pure
        @Override
        public final @Nullable W decodeNullable(@Nonnull Object none, @Nullable @NonEncoding Block block) throws InvalidEncodingException, InternalException {
            return block == null ? null : decodeNonNullable(none, block);
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull XDFConverter<W> getXDFConverter();
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * The SQL converter for wrappers.
     */
    @Immutable
    public abstract static class SQLConverter<W extends AbstractWrapper<W>> extends AbstractSQLConverter<W, Object> {
        
        /**
         * Stores the semantic type of the restored wrappers.
         */
        private final @Nonnull @Loaded SemanticType type;
        
        /**
         * Returns the semantic type of the restored wrappers.
         * 
         * @return the semantic type of the restored wrappers.
         */
        public final @Nonnull @Loaded SemanticType getType() {
            return type;
        }
        
        /**
         * Creates a new SQL converter with the given column declaration and semantic type.
         * 
         * @param declaration the declaration used to store instances of the wrapper.
         * @param type the semantic type of the restored wrappers.
         */
        protected SQLConverter(@Nonnull @NonNullableElements Declaration declaration, @Nonnull @Loaded SemanticType type) {
            super(declaration);
            
            this.type = type;
        }
        
        @Pure
        @Override
        public final void storeNonNullable(@Nonnull W wrapper, @NonCapturable @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
            values.set(index.getAndIncrementValue(), wrapper.toString());
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull SQLConverter<W> getSQLConverter();
    
    /**
     * Returns the value of this wrapper for SQL.
     * 
     * @return the value of this wrapper for SQL.
     */
    @Pure
    @Override
    public abstract @Nonnull String toString();
    
}
