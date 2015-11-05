package net.digitalid.service.core.converter.key;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;

/**
 * This class allows to cast objects to the specified subtype.
 * 
 * @param <S> the supertype from which the objects are downcast.
 * @param <O> the subtype to which the objects are downcast and returned.
 */
@Stateless
public abstract class ObjectCaster<S, O extends S> {
    
    /**
     * Casts the given object from the supertype to the specified subtype.
     * 
     * @param object the object of the supertype which is to be downcast.
     * 
     * @return the given object cast from the supertype to the specified type.
     * 
     * @throws InvalidEncodingException if the given object is not an instance of the specified subtype.
     */
    @Pure
    protected abstract @Nonnull O cast(@Nonnull S object) throws InvalidEncodingException;
    
}
