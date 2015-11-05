package net.digitalid.service.core.converter.xdf;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.converter.key.AbstractNonRequestingKeyConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class implements a non-requesting encoding factory that is based on another non-requesting encoding factory.
 * 
 * @param <O> the type of the objects that this factory can encode and decode, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to decode a block, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of a block, declare it as an {@link Object}.
 * @param <K> the type of the objects that the other factory encodes and decodes (usually as a key for the objects of this factory).
 * 
 * @see SubtypingNonRequestingEncodingFactory
 */
@Immutable
public class ChainingNonRequestingXDFConverter<O, E, K> extends AbstractNonRequestingXDFConverter<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory used to transform and reconstruct the object.
     */
    private final @Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K> objectFactory;
    
    /**
     * Stores the factory used to encode and decode the key.
     */
    private final @Nonnull AbstractNonRequestingXDFConverter<K, E> keyFactory;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new factory-based encoding factory with the given parameters.
     * 
     * @param type the semantic type that corresponds to the encoding class.
     * @param objectFactory the factory used to transform and reconstruct the object.
     * @param keyFactory the factory used to encode and decode the object's key.
     * 
     * @require type.isBasedOn(keyFactory.getType()) : "The given type is based on the type of the key factory.";
     */
    protected ChainingNonRequestingXDFConverter(@Nonnull SemanticType type, @Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K> objectFactory, @Nonnull AbstractNonRequestingXDFConverter<K, E> keyFactory) {
        super(type);
        
        assert type.isBasedOn(keyFactory.getType()) : "The given type is based on the type of the factory.";
        
        this.objectFactory = objectFactory;
        this.keyFactory = keyFactory;
    }
    
    /**
     * Creates a new factory-based encoding factory with the given parameters.
     * 
     * @param objectFactory the factory used to transform and reconstruct the object.
     * @param keyFactory the factory used to encode and decode the object's key.
     */
    @Pure
    public static @Nonnull <O, E, K> ChainingNonRequestingXDFConverter<O, E, K> get(@Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K> objectFactory, @Nonnull AbstractNonRequestingXDFConverter<K, E> keyFactory) {
        return new ChainingNonRequestingXDFConverter<>(keyFactory.getType(), objectFactory, keyFactory);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Nonnull Block encodeNonNullable(@Nonnull O object) {
        return keyFactory.encodeNonNullable(objectFactory.getKey(object)).setType(getType());
    }
    
    @Pure
    @Override
    public final @Nonnull O decodeNonNullable(@Nonnull E entity, @Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the type of this factory.";
        
        final @Nonnull K key = keyFactory.decodeNonNullable(entity, block);
        if (!objectFactory.isValid(key)) throw new InvalidEncodingException("The decoded key '" + key + "' is invalid.");
        return objectFactory.getObject(entity, key);
    }
    
}
