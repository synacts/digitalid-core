package net.digitalid.service.core.converter.key;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.encoding.InvalidClassCastException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;

/**
 * This class allows to cast objects to the specified subtype.
 * 
 * @param <S> the supertype from which the objects are downcast.
 * @param <O> the subtype to which the objects are downcast and returned.
 */
@Stateless
public abstract class Caster<S, O extends S> {
    
    /**
     * Casts the given object from the supertype to the specified subtype.
     * 
     * @param object the object of the supertype which is to be downcast.
     * 
     * @return the given object cast from the supertype to the specified subtype.
     * 
     * @throws InvalidEncodingException if the given object is not an instance of the specified subtype.
     */
    @Pure
    protected abstract @Nonnull O cast(@Nonnull S object) throws InvalidClassCastException;
    
}
