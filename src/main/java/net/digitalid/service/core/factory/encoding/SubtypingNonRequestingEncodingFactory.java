package net.digitalid.service.core.factory.encoding;

import javax.annotation.Nonnull;
import net.digitalid.service.core.factory.object.ObjectBasedObjectFactory;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class implements a non-requesting encoding factory that subtypes on another non-requesting encoding factory.
 * 
 * @param <O> the type of the objects that this factory can encode and decode, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to decode a block, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of a block, declare it as an {@link Object}.
 * 
 * @see ObjectBasedObjectFactory
 */
@Immutable
public final class SubtypingNonRequestingEncodingFactory<O, E> extends FactoryBasedNonRequestingEncodingFactory<O, E, O> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new subtyping encoding factory with the given parameters.
     * 
     * @param type the semantic type that is used for the encoded blocks.
     * @param factory the factory used to encode and decode the objects.
     * 
     * @require type.isBasedOn(factory.getType()) : "The given type is based on the type of the factory.";
     */
    private SubtypingNonRequestingEncodingFactory(@Nonnull SemanticType type, @Nonnull AbstractNonRequestingEncodingFactory<O, E> factory) {
        super(type, ObjectBasedObjectFactory.<O>get(), factory);
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
    public static @Nonnull <O, E> SubtypingNonRequestingEncodingFactory<O, E> get(@Nonnull SemanticType type, @Nonnull AbstractNonRequestingEncodingFactory<O, E> factory) {
        return new SubtypingNonRequestingEncodingFactory<>(type, factory);
    }
    
}
