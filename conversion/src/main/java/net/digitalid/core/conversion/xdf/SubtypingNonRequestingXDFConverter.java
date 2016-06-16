package net.digitalid.core.conversion.xdf;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.conversion.key.NonConvertingKeyConverter;
import net.digitalid.core.identity.SemanticType;

/**
 * This class implements a non-requesting XDF converter that subtypes on another non-requesting XDF converter.
 * 
 * @param <O> the type of the objects that this converter can encode and decode, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to decode a block, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of a block, declare it as an {@link Object}.
 * 
 * @see NonConvertingKeyConverter
 */
@Immutable
public final class SubtypingNonRequestingXDFConverter<O, E> extends ChainingNonRequestingXDFConverter<O, E, O, E> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new subtyping XDF converter with the given parameters.
     * 
     * @param type the semantic type that is used for the encoded blocks.
     * @param XDFConverter the XDF converter used to encode and decode the objects.
     * 
     * @require type.isBasedOn(XDFConverter.getType()) : "The given type is based on the type of the XDF converter.";
     */
    private SubtypingNonRequestingXDFConverter(@Nonnull SemanticType type, @Nonnull NonRequestingXDFConverter<O, E> XDFConverter) {
        super(type, NonConvertingKeyConverter.<O, E>get(), XDFConverter);
    }
    
    /**
     * Returns a new subtyping XDF converter with the given parameters.
     * 
     * @param type the semantic type that is used for the encoded blocks.
     * @param XDFConverter the XDF converter used to encode and decode the objects.
     * 
     * @return a new subtyping XDF converter with the given parameters.
     * 
     * @require type.isBasedOn(XDFConverter.getType()) : "The given type is based on the type of the XDF converter.";
     */
    @Pure
    public static @Nonnull <O, E> SubtypingNonRequestingXDFConverter<O, E> get(@Nonnull SemanticType type, @Nonnull NonRequestingXDFConverter<O, E> XDFConverter) {
        return new SubtypingNonRequestingXDFConverter<>(type, XDFConverter);
    }
    
}
