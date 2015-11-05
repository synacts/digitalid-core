package net.digitalid.service.core.concept;

import javax.annotation.Nonnull;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.converter.key.AbstractNonRequestingKeyConverter;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * Description.
 */
@Immutable
public final class ConceptKeyConverter<C extends Concept<C, E, K>, E extends Entity<E>, K> extends AbstractNonRequestingKeyConverter<C, E, K> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– ConceptIndex –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new concept factory with the given index.
     * 
     * @param index the index that caches existing concepts and creates new ones.
     */
    private ConceptKeyConverter(@Nonnull ConceptIndex<C, E, K> index) {
        this.index = index;
    }
    
    /**
     * Creates a new concept factory with the given index.
     * 
     * @param index the index that caches existing concepts and creates new ones.
     * 
     * @return a new concept factory with the given index.
     */
    @Pure
    public static @Nonnull <C extends Concept<C, E, K>, E extends Entity<E>, K> ConceptKeyConverter<C, E, K> get(@Nonnull ConceptIndex<C, E, K> index) {
        return new ConceptKeyConverter<>(index);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Conversions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull K getKey(@Nonnull C concept) {
        return concept.getKey();
    }
    
    @Pure
    @Override
    public @Nonnull C getObject(@Nonnull E entity, @Nonnull K key) {
        return index.get(entity, key);
    }
    
}
