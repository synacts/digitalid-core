package net.digitalid.service.core.factory.encoding;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.factory.object.AbstractNonRequestingObjectFactory;
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
public class FactoryBasedNonRequestingEncodingFactory<O, E, K> extends AbstractNonRequestingEncodingFactory<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory used to transform and reconstruct the object.
     */
    private final @Nonnull AbstractNonRequestingObjectFactory<O, ? super E, K> objectFactory;
    
    /**
     * Stores the factory used to encode and decode the key.
     */
    private final @Nonnull AbstractNonRequestingEncodingFactory<K, E> keyFactory;
    
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
    protected FactoryBasedNonRequestingEncodingFactory(@Nonnull SemanticType type, @Nonnull AbstractNonRequestingObjectFactory<O, ? super E, K> objectFactory, @Nonnull AbstractNonRequestingEncodingFactory<K, E> keyFactory) {
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
    public static @Nonnull <O, E, K> FactoryBasedNonRequestingEncodingFactory<O, E, K> get(@Nonnull AbstractNonRequestingObjectFactory<O, ? super E, K> objectFactory, @Nonnull AbstractNonRequestingEncodingFactory<K, E> keyFactory) {
        return new FactoryBasedNonRequestingEncodingFactory<>(keyFactory.getType(), objectFactory, keyFactory);
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
