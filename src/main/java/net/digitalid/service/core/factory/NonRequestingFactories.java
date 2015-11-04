package net.digitalid.service.core.factory;

import javax.annotation.Nonnull;
import net.digitalid.service.core.factory.encoding.AbstractNonRequestingEncodingFactory;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.storing.AbstractStoringFactory;

/**
 * This class allows to store several non-requesting factories in a single object.
 * 
 * @param <O> the type of the objects that the factories can convert, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to reconstruct an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the reconstruction of an object, declare it as an {@link Object}.
 */
@Immutable
public final class NonRequestingFactories<O, E> extends Factories<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull AbstractNonRequestingEncodingFactory<O, E> getEncodingFactory() {
        return (AbstractNonRequestingEncodingFactory<O, E>) super.getEncodingFactory();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new object with the given factories.
     * 
     * @param encodingFactory the encoding factory.
     * @param storingFactory the storing factory.
     */
    private NonRequestingFactories(@Nonnull AbstractNonRequestingEncodingFactory<O, E> encodingFactory, @Nonnull AbstractStoringFactory<O, E> storingFactory) {
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
    public static @Nonnull <O, E> NonRequestingFactories<O, E> get(@Nonnull AbstractNonRequestingEncodingFactory<O, E> encodingFactory, @Nonnull AbstractStoringFactory<O, E> storingFactory) {
        return new NonRequestingFactories<>(encodingFactory, storingFactory);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Subtyping –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull NonRequestingFactories<O, E> setType(@Nonnull SemanticType type) {
        return new NonRequestingFactories<>(getEncodingFactory().setType(type), getStoringFactory());
    }
    
}
