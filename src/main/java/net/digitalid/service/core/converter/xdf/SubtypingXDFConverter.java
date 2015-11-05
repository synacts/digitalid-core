package net.digitalid.service.core.converter.xdf;

import javax.annotation.Nonnull;
import net.digitalid.service.core.converter.key.NonConvertingKeyConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class implements an encoding factory that subtypes on another encoding factory.
 * 
 * @param <O> the type of the objects that this factory can encode and decode, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to decode a block, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of a block, declare it as an {@link Object}.
 * 
 * @see NonConvertingKeyConverter
 */
@Immutable
public final class SubtypingXDFConverter<O, E> extends ChainingXDFConverter<O, E, O> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new subtyping encoding factory with the given parameters.
     * 
     * @param type the semantic type that is used for the encoded blocks.
     * @param factory the factory used to encode and decode the objects.
     * 
     * @require type.isBasedOn(factory.getType()) : "The given type is based on the type of the factory.";
     */
    private SubtypingXDFConverter(@Nonnull SemanticType type, @Nonnull AbstractXDFConverter<O, E> factory) {
        super(type, NonConvertingKeyConverter.<O>get(), factory);
    }
    
    /**
     * Creates a new subtyping encoding factory with the given parameters.
     * 
     * @param type the semantic type that is used for the encoded blocks.
     * @param factory the factory used to encode and decode the objects.
     * 
     * @return a new subtyping encoding factory with the given parameters.
     * 
     * @require type.isBasedOn(factory.getType()) : "The given type is based on the type of the factory.";
     */
    @Pure
    public static @Nonnull <O, E> SubtypingXDFConverter<O, E> get(@Nonnull SemanticType type, @Nonnull AbstractXDFConverter<O, E> factory) {
        return new SubtypingXDFConverter<>(type, factory);
    }
    
}
