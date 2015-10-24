package net.digitalid.service.core.concept;

import javax.annotation.Nonnull;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.storing.AbstractStoringFactory;
import net.digitalid.utility.database.storing.FactoryBasedStoringFactory;

/**
 * The storing factory for {@link Concept concepts}.
 */
@Immutable
public final class ConceptStoringFactory<C extends Concept<C, E, K>, E extends Entity<E>, K> extends FactoryBasedStoringFactory<C, E, K> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Index –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the index that caches existing concepts.
     */
    private final @Nonnull Index<C, E, K> index;
    
    /**
     * Returns the index that caches existing concepts.
     * 
     * @return the index that caches existing concepts.
     */
    @Pure
    public @Nonnull Index<C, E, K> getIndex() {
        return index;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new storing factory based on the given key factory.
     * 
     * @param keyFactory the factory to store and restore the key of the concept.
     * @param index the index that caches existing concepts and creates new ones.
     */
    private ConceptStoringFactory(@Nonnull AbstractStoringFactory<K, E> keyFactory, @Nonnull Index<C, E, K> index) {
        super(keyFactory);
        
        this.index = index;
    }
    
    /**
     * Creates a new storing factory based on the given key factory.
     * 
     * @param keyFactory the factory to store and restore the key of the concept.
     * @param index the index that caches existing concepts and creates new ones.
     * 
     * @return a new storing factory based on the given key factory.
     */
    @Pure
    public static @Nonnull <C extends Concept<C, E, K>, E extends Entity<E>, K> ConceptStoringFactory<C, E, K> get(@Nonnull AbstractStoringFactory<K, E> keyFactory, @Nonnull Index<C, E, K> index) {
        return new ConceptStoringFactory<>(keyFactory, index);
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
