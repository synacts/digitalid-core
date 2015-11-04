package net.digitalid.service.core.factory.object;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This class allows to transform an object to its key and reconstruct it again given its key (and an external object) without requests.
 * 
 * @param <O> the type of the objects that this factory can transform and reconstruct, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to reconstruct an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the reconstruction of an object, declare it as an {@link Object}.
 * @param <K> the type of the keys which the objects are transformed to and reconstructed from (with an external object).
 * 
 * @see IdentityObjectFactory
 */
@Stateless
public abstract class AbstractNonRequestingObjectFactory<O, E, K> extends AbstractObjectFactory<O, E, K> {
    
    @Pure
    @Override
    public abstract @Nonnull O getObject(@Nonnull E entity, @Nonnull @Validated K key) throws InvalidEncodingException;
    
}
