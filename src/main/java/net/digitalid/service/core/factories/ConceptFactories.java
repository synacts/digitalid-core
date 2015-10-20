package net.digitalid.service.core.factories;

import javax.annotation.Nonnull;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class allows to store several concept factories in a single object.
 * 
 * @param <C> the type of the objects that the factories can convert, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to reconstruct an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the reconstruction of an object, declare it as an {@link Object}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class ConceptFactories<C extends Concept<C, E, ?>, E extends Entity<E>> extends GenericFactories<C, E, Concept.EncodingFactory<C, E, ?>, Concept.StoringFactory<C, E, ?>> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new object with the given factories.
     * 
     * @param encodingFactory the encoding factory.
     * @param storingFactory the storing factory.
     */
    private ConceptFactories(@Nonnull Concept.EncodingFactory<C, E, ?> encodingFactory, @Nonnull Concept.StoringFactory<C, E, ?> storingFactory) {
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
    public static @Nonnull <C extends Concept<C, E, ?>, E extends Entity<E>> ConceptFactories<C, E> get(@Nonnull Concept.EncodingFactory<C, E, ?> encodingFactory, @Nonnull Concept.StoringFactory<C, E, ?> storingFactory) {
        return new ConceptFactories<>(encodingFactory, storingFactory);
    }
    
}
