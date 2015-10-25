package net.digitalid.service.core.factories;

import javax.annotation.Nonnull;
import net.digitalid.service.core.encoding.AbstractEncodingFactory;
import net.digitalid.utility.database.storing.AbstractStoringFactory;
import net.digitalid.utility.annotations.state.Immutable;

/**
 * This class allows to store several generic factories in a single object.
 * 
 * @param <O> the type of the objects that the factories can convert, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to reconstruct an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the reconstruction of an object, declare it as an {@link Object}.
 * @param <EF> the type of encoding factory that this class encapsulates.
 * @param <SF> the type of storing factory that this class encapsulates.
 */
@Immutable
public abstract class GenericFactories<O, E, EF extends AbstractEncodingFactory<O, E>, SF extends AbstractStoringFactory<O, E>> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the encoding factory.
     */
    private final @Nonnull EF encodingFactory;
    
    /**
     * Returns the encoding factory.
     * 
     * @return the encoding factory.
     */
    public @Nonnull EF getEncodingFactory() {
        return encodingFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storing Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the storing factory.
     */
    private final @Nonnull SF storingFactory;
    
    /**
     * Returns the storing factory.
     * 
     * @return the storing factory.
     */
    public @Nonnull SF getStoringFactory() {
        return storingFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new object with the given factories.
     * 
     * @param encodingFactory the encoding factory.
     * @param storingFactory the storing factory.
     */
    protected GenericFactories(@Nonnull EF encodingFactory, @Nonnull SF storingFactory) {
        this.encodingFactory = encodingFactory;
        this.storingFactory = storingFactory;
    }
    
}
