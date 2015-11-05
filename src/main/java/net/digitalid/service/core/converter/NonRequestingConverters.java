package net.digitalid.service.core.converter;

import javax.annotation.Nonnull;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

/**
 * This class allows to store several non-requesting factories in a single object.
 * 
 * @param <O> the type of the objects that the factories can convert, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to reconstruct an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the reconstruction of an object, declare it as an {@link Object}.
 */
@Immutable
public final class NonRequestingConverters<O, E> extends Converters<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull AbstractNonRequestingXDFConverter<O, E> getEncodingFactory() {
        return (AbstractNonRequestingXDFConverter<O, E>) super.getEncodingFactory();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new object with the given factories.
     * 
     * @param encodingFactory the encoding factory.
     * @param storingFactory the storing factory.
     */
    private NonRequestingConverters(@Nonnull AbstractNonRequestingXDFConverter<O, E> encodingFactory, @Nonnull AbstractSQLConverter<O, E> storingFactory) {
        super(encodingFactory, storingFactory);
    }
    
    /**
     * Creates a new object with the given factories.
     * 
     * @param encodingFactory the encoding factory.
     * @param storingFactory the storing factory.
     * 
     * @return a new object with the given factories.
     */
    @Pure
    public static @Nonnull <O, E> NonRequestingConverters<O, E> get(@Nonnull AbstractNonRequestingXDFConverter<O, E> encodingFactory, @Nonnull AbstractSQLConverter<O, E> storingFactory) {
        return new NonRequestingConverters<>(encodingFactory, storingFactory);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Subtyping –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull NonRequestingConverters<O, E> setType(@Nonnull SemanticType type) {
        return new NonRequestingConverters<>(getEncodingFactory().setType(type), getStoringFactory());
    }
    
}
