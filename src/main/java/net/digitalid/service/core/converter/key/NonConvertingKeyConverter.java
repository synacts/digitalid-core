package net.digitalid.service.core.converter.key;

import javax.annotation.Nonnull;
import net.digitalid.service.core.converter.xdf.SubtypingXDFConverter;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;

/**
 * This class implements an object factory that uses the object itself as its key.
 * 
 * @param <O> the type of the objects that this factory can transform and reconstruct.
 * 
 * @see SubtypingXDFConverter
 */
@Stateless
public final class NonConvertingKeyConverter<O> extends AbstractNonRequestingKeyConverter<O, Object, O> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new object-based object factory.
     */
    private NonConvertingKeyConverter() {}
    
    /**
     * Returns a new object-based object factory.
     * 
     * @return a new object-based object factory.
     */
    @Pure
    public static @Nonnull <O> NonConvertingKeyConverter<O> get() {
        return new NonConvertingKeyConverter<>();
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
