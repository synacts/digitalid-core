package net.digitalid.service.core.factory.object;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This class allows to transform an object to its key and reconstruct it again given its key (and an external object).
 * 
 * @param <O> the type of the objects that this factory can transform and reconstruct, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to reconstruct an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the reconstruction of an object, declare it as an {@link Object}.
 * @param <K> the type of the keys which the objects are transformed to and reconstructed from (with an external object).
 * 
 * @see NonRequestingObjectFactory
 */
@Stateless
public abstract class AbstractObjectFactory<O, E, K> {
    
    /**
     * Returns whether the given key is valid.
     * 
     * @param key the key to be checked.
     * 
     * @return whether the given key is valid.
     */
    @Pure
    public boolean isValid(@Nonnull K key) {
        return true;
    }
    
    /**
     * Returns the key of the given object.
     * 
     * @param object the object whose key is to be returned.
     * 
     * @return the key of the given object.
     */
    @Pure
    public abstract @Nonnull @Validated K getKey(@Nonnull O object);
    
    /**
     * Returns the object with the given key.
     * 
     * @param entity the entity needed to decode the object.
     * @param key the key which denotes the returned object.
     * 
     * @return the object with the given key.
     */
    @Pure
    public abstract @Nonnull O getObject(@Nonnull E entity, @Nonnull @Validated K key) throws AbortException, PacketException, ExternalException, NetworkException;
    
}
