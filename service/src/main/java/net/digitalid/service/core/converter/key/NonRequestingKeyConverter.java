package net.digitalid.service.core.converter.key;

import javax.annotation.Nonnull;
import net.digitalid.database.core.converter.key.KeyConverter;
import net.digitalid.service.core.concept.ConceptKeyConverter;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

/**
 * This class allows to convert an object to its key and recover it again given its key (and an external object) without requests.
 * 
 * @param <O> the type of the objects that this converter can convert and recover, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to recover an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of an object, declare it as an {@link Object}.
 * @param <K> the type of the keys which the objects are converted to and recovered from (with an external object).
 * @param <D> the type of the external object that is needed to recover the key, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of the key, declare it as an {@link Object}.
 * 
 * @see NonConvertingKeyConverter
 * @see XDFBasedKeyConverter
 * @see ConceptKeyConverter
 */
@Stateless
public abstract class NonRequestingKeyConverter<O, E, K, D> extends RequestingKeyConverter<O, E, K, D> implements KeyConverter<O, E, K, D> {
    
    @Pure
    @Override
    public abstract @Nonnull O recover(@Nonnull E external, @Nonnull @Validated K key) throws InvalidEncodingException, InternalException;
    
}
