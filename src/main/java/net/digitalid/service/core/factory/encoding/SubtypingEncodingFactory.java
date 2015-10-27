package net.digitalid.service.core.factory.encoding;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class implements an encoding factory that subtypes on another encoding factory.
 * 
 * @param <O> the type of the objects that this factory can encode and decode, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to decode a block, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of a block, declare it as an {@link Object}.
 */
@Immutable
public final class SubtypingEncodingFactory<O, E> extends FactoryBasedEncodingFactory<O, E, O> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new subtyping encoding factory with the given parameters.
     * 
     * @param type the semantic type that is used for the encoded blocks.
     * @param factory the factory used to encode and decode the objects.
     * 
     * @require type.isBasedOn(factory.getType()) : "The given type is based on the type of the factory.";
     */
    private SubtypingEncodingFactory(@Nonnull SemanticType type, @Nonnull AbstractEncodingFactory<O, E> factory) {
        super(type, factory);
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
    public static @Nonnull <O, E> SubtypingEncodingFactory<O, E> get(@Nonnull SemanticType type, @Nonnull AbstractEncodingFactory<O, E> factory) {
        return new SubtypingEncodingFactory<>(type, factory);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Conversions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull O getKey(@Nonnull O object) {
        return object;
    }
    
    @Pure
    @Override
    public @Nonnull O getObject(@Nonnull E entity, @Nonnull O key) {
        return key;
    }
    
}
