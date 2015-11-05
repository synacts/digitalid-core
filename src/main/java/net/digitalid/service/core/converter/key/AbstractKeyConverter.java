package net.digitalid.service.core.converter.key;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This class allows to convert an object to its key and recover it again given its key (and an external object).
 * 
 * @param <O> the type of the objects that this converter can convert and recover, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to recover an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of an object, declare it as an {@link Object}.
 * @param <K> the type of the keys which the objects are converted to and recovered from (with an external object).
 * 
 * @see AbstractNonRequestingKeyConverter
 */
@Stateless
public abstract class AbstractKeyConverter<O, E, K> {
    
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
    public abstract @Nonnull @Validated K convert(@Nonnull O object);
    
    /**
     * Returns the object with the given key.
     * 
     * @param external the external object needed to recover the object.
     * @param key the key which denotes the returned object.
     * 
     * @return the object with the given key.
     */
    @Pure
    public abstract @Nonnull O recover(@Nonnull E external, @Nonnull @Validated K key) throws AbortException, PacketException, ExternalException, NetworkException;
    
}
