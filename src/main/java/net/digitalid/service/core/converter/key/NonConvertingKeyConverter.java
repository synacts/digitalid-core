package net.digitalid.service.core.converter.key;

import javax.annotation.Nonnull;
import net.digitalid.service.core.converter.xdf.SubtypingXDFConverter;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;

/**
 * This class implements a key converter that uses the object itself as its key.
 * 
 * @param <O> the type of the objects that this converter can convert and recover.
 * @param <E> the type of the external object that is needed to recover an object.
 * 
 * @see SubtypingXDFConverter
 */
@Stateless
public final class NonConvertingKeyConverter<O, E> extends AbstractNonRequestingKeyConverter<O, E, O, E> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
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
    public static @Nonnull <O, E> NonConvertingKeyConverter<O, E> get() {
        return new NonConvertingKeyConverter<>();
    }
    
    /* -------------------------------------------------- Conversions -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull E decompose(@Nonnull E external) {
        return external;
    }
    
    @Pure
    @Override
    public @Nonnull O convert(@Nonnull O object) {
        return object;
    }
    
    @Pure
    @Override
    public @Nonnull O recover(@Nonnull E external, @Nonnull O object) {
        return object;
    }
    
}
