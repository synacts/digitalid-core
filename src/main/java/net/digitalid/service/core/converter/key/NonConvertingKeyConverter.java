package net.digitalid.service.core.converter.key;

import javax.annotation.Nonnull;
import net.digitalid.service.core.converter.xdf.SubtypingXDFConverter;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;

/**
 * This class implements a key converter that uses the object itself as its key.
 * 
 * @param <O> the type of the objects that this converter can convert and recover.
 * 
 * @see SubtypingXDFConverter
 */
@Stateless
public final class NonConvertingKeyConverter<O> extends AbstractNonRequestingKeyConverter<O, Object, O> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new non-converting key converter.
     */
    private NonConvertingKeyConverter() {}
    
    /**
     * Returns a new non-converting key converter.
     * 
     * @return a new non-converting key converter.
     */
    @Pure
    public static @Nonnull <O> NonConvertingKeyConverter<O> get() {
        return new NonConvertingKeyConverter<>();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull O convert(@Nonnull O object) {
        return object;
    }
    
    @Pure
    @Override
    public @Nonnull O recover(@Nonnull Object none, @Nonnull O key) {
        return key;
    }
    
}
