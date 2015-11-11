package net.digitalid.service.core.converter.key;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This class allows to convert an object to its key and recover it again by downcasting the object returned by the abstract method with the given caster.
 * 
 * @param <O> the type of the objects that this converter can convert and recover, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to recover an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of an object, declare it as an {@link Object}.
 * @param <K> the type of the keys which the objects are converted to and recovered from (with an external object).
 * @param <D> the type of the external object that is needed to recover the key, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of the key, declare it as an {@link Object}.
 * @param <S> the supertype from which the objects returned by the abstract (and thus overridden) method are downcast.
 */
@Immutable
public abstract class CastingNonRequestingKeyConverter<O extends S, E, K, D, S> extends AbstractNonRequestingKeyConverter<O, E, K, D> {
    
    /* -------------------------------------------------- Caster -------------------------------------------------- */
    
    /**
     * Stores the caster that allows to cast objects to the specified subtype.
     */
    private final @Nonnull Caster<S, O> caster;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new casting non-requesting key converter with the given caster.
     * 
     * @param caster the caster that allows to cast objects to the specified subtype.
     */
    protected CastingNonRequestingKeyConverter(@Nonnull Caster<S, O> caster) {
        this.caster = caster;
    }
    
    /* -------------------------------------------------- Abstract -------------------------------------------------- */
    
    /**
     * Returns the object of the supertype with the given key.
     * 
     * @param external the external object needed to recover the object.
     * @param key the key which denotes the returned object.
     * 
     * @return the object of the supertype with the given key.
     */
    @Pure
    protected abstract @Nonnull S recoverSupertype(@Nonnull E external, @Nonnull @Validated K key) throws InvalidEncodingException;
    
    /* -------------------------------------------------- Method -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull O recover(@Nonnull E external, @Nonnull K key) throws InvalidEncodingException {
        return caster.cast(recoverSupertype(external, key));
    }
    
}
