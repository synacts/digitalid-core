package net.digitalid.service.core.concept;

import javax.annotation.Nonnull;
import net.digitalid.service.core.converter.key.AbstractNonRequestingKeyConverter;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This class allows to convert an object to its key and recover it again given its key (and an external object) without requests.
 * 
 * @param <O> the type of the objects that this converter can convert and recover, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to recover an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of an object, declare it as an {@link Object}.
 * @param <K> the type of the keys which the objects are converted to and recovered from (with an external object).
 */
@Immutable
public final class ConceptKeyConverter<C extends Concept<C, E, K>, E extends Entity, K> extends AbstractNonRequestingKeyConverter<C, E, K, E> {
    
    /* -------------------------------------------------- Concept Index -------------------------------------------------- */
    
    /**
     * Stores the index that caches existing concepts.
     */
    private final @Nonnull ConceptIndex<C, E, K> index;
    
    /**
     * Returns the index that caches existing concepts.
     * 
     * @return the index that caches existing concepts.
     */
    @Pure
    public @Nonnull ConceptIndex<C, E, K> getIndex() {
        return index;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new concept-key converter with the given index.
     * 
     * @param index the index that caches existing concepts and creates new ones.
     */
    private ConceptKeyConverter(@Nonnull ConceptIndex<C, E, K> index) {
        this.index = index;
    }
    
    /**
     * Creates a new concept-key converter with the given index.
     * 
     * @param index the index that caches existing concepts and creates new ones.
     * 
     * @return a new concept-key converter with the given index.
     */
    @Pure
    public static @Nonnull <C extends Concept<C, E, K>, E extends Entity, K> ConceptKeyConverter<C, E, K> get(@Nonnull ConceptIndex<C, E, K> index) {
        return new ConceptKeyConverter<>(index);
    }
    
    /* -------------------------------------------------- Conversions -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @Validated K convert(@Nonnull C concept) {
        return concept.getKey();
    }
    
    @Pure
    @Override
    public @Nonnull C recover(@Nonnull E external, @Nonnull @Validated K key) {
        return index.get(external, key);
    }
    
}
