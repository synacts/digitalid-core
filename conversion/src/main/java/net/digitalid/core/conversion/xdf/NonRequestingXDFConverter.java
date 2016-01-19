package net.digitalid.core.conversion.xdf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.annotations.NonEncoding;

import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.identity.annotations.Loaded;

/**
 * This class is like {@link RequestingXDFConverter} except that the decoding of {@link Block blocks} throws less exceptions.
 * This converter does not allow file, network and database requests during {@link #decodeNonNullable(java.lang.Object, net.digitalid.service.core.block.Block) decoding}.
 */
@Immutable
public abstract class NonRequestingXDFConverter<O, E> extends RequestingXDFConverter<O, E> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull NonRequestingXDFConverter<O, E> setType(@Nonnull SemanticType type) {
        return SubtypingNonRequestingXDFConverter.get(type, this);
    }
    
    /* -------------------------------------------------- Decoding -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull O decodeNonNullable(@Nonnull E external, @Nonnull @NonEncoding Block block) throws InvalidEncodingException, InternalException;
    
    @Pure
    @Override
    public final @Nullable O decodeNullable(@Nonnull E external, @Nullable @NonEncoding Block block) throws InvalidEncodingException, InternalException {
        return block == null ? null : decodeNonNullable(external, block);
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new abstract non-requesting XDF converter with the given type.
     * 
     * @param type the semantic type that corresponds to the class that implements XDF.
     */
    protected NonRequestingXDFConverter(@Nonnull @Loaded SemanticType type) {
        super(type);
    }
    
}
