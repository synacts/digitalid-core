package net.digitalid.service.core.factory.object;

import javax.annotation.Nonnull;
import net.digitalid.service.core.factory.encoding.SubtypingEncodingFactory;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;

/**
 * This class implements an object factory that uses the object itself as its key.
 * 
 * @param <O> the type of the objects that this factory can transform and reconstruct.
 * 
 * @see SubtypingEncodingFactory
 */
@Stateless
public final class ObjectBasedObjectFactory<O> extends AbstractNonRequestingObjectFactory<O, Object, O> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new object-based object factory.
     */
    private ObjectBasedObjectFactory() {}
    
    /**
     * Returns a new object-based object factory.
     * 
     * @return a new object-based object factory.
     */
    @Pure
    public static @Nonnull <O> ObjectBasedObjectFactory<O> get() {
        return new ObjectBasedObjectFactory<>();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull O getKey(@Nonnull O object) {
        return object;
    }
    
    @Pure
    @Override
    public @Nonnull O getObject(@Nonnull Object none, @Nonnull O key) {
        return key;
    }
    
}
