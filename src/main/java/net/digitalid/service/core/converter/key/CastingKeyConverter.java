package net.digitalid.service.core.converter.key;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This class allows to transform an object to its key and reconstruct it again by downcasting the object returned by the abstract method with the given caster.
 * 
 * @param <O> the type of the objects that this factory can transform and reconstruct, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to reconstruct an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the reconstruction of an object, declare it as an {@link Object}.
 * @param <K> the type of the keys which the objects are transformed to and reconstructed from (with an external object).
 * @param <S> the supertype from which the objects returned by the abstract (and thus overridden) method are downcast.
 */
@Immutable
public abstract class CastingKeyConverter<O extends S, E, K, S> extends AbstractKeyConverter<O, E, K> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Caster –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the caster that allows to cast objects to the specified subtype.
     */
    private final @Nonnull ObjectCaster<S, O> caster;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new casting object factory with the given caster.
     * 
     * @param caster the caster that allows to cast objects to the specified subtype.
     */
    protected CastingKeyConverter(@Nonnull ObjectCaster<S, O> caster) {
        this.caster = caster;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Abstract –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the object of the supertype with the given key.
     * 
     * @param entity the entity needed to decode the object.
     * @param key the key which denotes the returned object.
     * 
     * @return the object of the supertype with the given key.
     */
    @Pure
    protected abstract @Nonnull S getObjectOfSupertype(@Nonnull E entity, @Nonnull @Validated K key) throws AbortException, PacketException, ExternalException, NetworkException;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Method –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Nonnull O getObject(@Nonnull E entity, @Nonnull K key) throws AbortException, PacketException, ExternalException, NetworkException {
        return caster.cast(getObjectOfSupertype(entity, key));
    }
    
}
