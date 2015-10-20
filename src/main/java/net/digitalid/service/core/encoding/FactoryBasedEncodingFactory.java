package net.digitalid.service.core.encoding;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class implements an encoding factory that is based on another encoding factory.
 * 
 * @param <O> the type of the objects that this factory can encode and decode, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to decode a block, which is quite often an {@link Entity}.
 *            In case no external information is needed for the decoding of a block, declare it as an {@link Object}.
 * @param <K> the type of the objects that the other factory encodes and decodes (usually as a key for the objects of this factory).
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class FactoryBasedEncodingFactory<O, E, K> extends AbstractEncodingFactory<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory used to encode and decode the key.
     */
    private final @Nonnull AbstractEncodingFactory<K, E> keyFactory;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new factory based encoding factory with the given parameters.
     * 
     * @param type the semantic type that corresponds to the encoding class.
     * @param keyFactory the factory used to encode and decode the object's key.
     * 
     * @require type.isBasedOn(factory.getType()) : "The given type is based on the type of the factory.";
     */
    protected FactoryBasedEncodingFactory(@Nonnull @Loaded SemanticType type, @Nonnull AbstractEncodingFactory<K, E> keyFactory) {
        super(type);
        
        assert type.isBasedOn(keyFactory.getType()) : "The given type is based on the type of the factory.";
        
        this.keyFactory = keyFactory;
    }
    
    /**
     * Creates a new factory based encoding factory with the given key factory.
     * 
     * @param keyFactory the factory used to encode and decode the object's key.
     */
    protected FactoryBasedEncodingFactory(@Nonnull AbstractEncodingFactory<K, E> keyFactory) {
        this(keyFactory.getType(), keyFactory);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Abstract –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the key of the given object.
     * 
     * @param object the object whose key is to be returned.
     * 
     * @return the key of the given object.
     */
    @Pure
    public abstract @Nonnull K getKey(@Nonnull O object);
    
    /**
     * Returns the object with the given key.
     * 
     * @param entity the entity needed to decode the object.
     * @param key the key which denotes the returned object.
     * 
     * @return the object with the given key.
     */
    @Pure
    public abstract @Nonnull O getObject(@Nonnull E entity, @Nonnull K key);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Nonnull Block encodeNonNullable(@Nonnull O object) {
        return keyFactory.encodeNonNullable(getKey(object)).setType(getType());
    }
    
    @Pure
    @Override
    public @Nonnull O decodeNonNullable(@Nonnull E entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the type of this factory.";
        
        return getObject(entity, keyFactory.decodeNonNullable(entity, block));
    }
    
}
