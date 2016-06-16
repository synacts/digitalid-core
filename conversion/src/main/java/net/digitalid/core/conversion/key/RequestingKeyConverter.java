package net.digitalid.core.conversion.key;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;

/**
 * This class allows to convert an object to its key and recover it again given its key (and an external object).
 * 
 * @param <O> the type of the objects that this converter can convert and recover, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to recover an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of an object, declare it as an {@link Object}.
 * @param <K> the type of the keys which the objects are converted to and recovered from (with an external object).
 * @param <D> the type of the external object that is needed to recover the key, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of the key, declare it as an {@link Object}.
 * 
 * @see AbstractNonRequestingKeyConverter
 */
@Stateless
public abstract class RequestingKeyConverter<O, E, K, D> {
    
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
     * Decomposes the external object into what is needed to recover the key.
     * 
     * @param external the external object needed to recover the object.
     * 
     * @return the external object which is needed to recover the key.
     */
    @Pure
    @SuppressWarnings("unchecked")
    public @Nonnull D decompose(@Nonnull E external) {
        return (D) external;
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
    public abstract @Nonnull O recover(@Nonnull E external, @Nonnull @Validated K key) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
    
}
