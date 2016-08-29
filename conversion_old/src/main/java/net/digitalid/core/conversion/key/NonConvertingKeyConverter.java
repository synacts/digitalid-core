package net.digitalid.core.conversion.key;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.conversion.xdf.SubtypingRequestingXDFConverter;

/**
 * This class implements a key converter that uses the object itself as its key.
 * 
 * @param <O> the type of the objects that this converter can convert and recover.
 * @param <E> the type of the external object that is needed to recover an object.
 * 
 * @see SubtypingRequestingXDFConverter
 */
@Stateless
public final class NonConvertingKeyConverter<O, E> extends NonRequestingKeyConverter<O, E, O, E> {
    
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
    public @Nonnull O convert(@Nonnull O object) {
        return object;
    }
    
    @Pure
    @Override
    public @Nonnull O recover(@Nonnull E external, @Nonnull O object) {
        return object;
    }
    
}
