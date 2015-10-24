package net.digitalid.service.core.factories;

import javax.annotation.Nonnull;
import net.digitalid.service.core.encoding.AbstractEncodingFactory;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.utility.database.storing.AbstractStoringFactory;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class allows to store several factories in a single object.
 * 
 * @param <O> the type of the objects that the factories can convert, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to reconstruct an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the reconstruction of an object, declare it as an {@link Object}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class Factories<O, E> extends GenericFactories<O, E, AbstractEncodingFactory<O, E>, AbstractStoringFactory<O, E>> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new object with the given factories.
     * 
     * @param encodingFactory the encoding factory.
     * @param storingFactory the storing factory.
     */
    private Factories(@Nonnull AbstractEncodingFactory<O, E> encodingFactory, @Nonnull AbstractStoringFactory<O, E> storingFactory) {
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
    public static @Nonnull <O, E> Factories<O, E> get(@Nonnull AbstractEncodingFactory<O, E> encodingFactory, @Nonnull AbstractStoringFactory<O, E> storingFactory) {
        return new Factories<>(encodingFactory, storingFactory);
    }
    
}
