package net.digitalid.service.core.concept;

import net.digitalid.service.core.identity.annotations.Loaded;

import net.digitalid.service.core.factory.encoding.AbstractEncodingFactory;
import net.digitalid.service.core.factory.encoding.FactoryBasedEncodingFactory;
import javax.annotation.Nonnull;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * The encoding factory for {@link Concept concepts}.
 */
@Immutable
public final class ConceptEncodingFactory<C extends Concept<C, E, K>, E extends Entity<E>, K> extends FactoryBasedEncodingFactory<C, E, K> {
    
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
     * Creates a new concept encoding factory based on the given key factory.
     * 
     * @param type the semantic type that corresponds to the encodable class.
     * @param keyFactory the factory to encode and decode the key of the concept.
     * @param index the index that caches existing concepts and creates new ones.
     * 
     * @require type.isBasedOn(factory.getType()) : "The given type is based on the type of the factory.";
     */
    private ConceptEncodingFactory(@Nonnull @Loaded SemanticType type, @Nonnull AbstractEncodingFactory<K, E> keyFactory, @Nonnull Index<C, E, K> index) {
        super(type, keyFactory);
        
        this.index = index;
    }
    
    /**
     * Creates a new concept encoding factory based on the given key factory.
     * 
     * @param type the semantic type that corresponds to the encodable class.
     * @param keyFactory the factory to encode and decode the key of the concept.
     * @param index the index that caches existing concepts and creates new ones.
     * 
     * @return a new concept encoding factory based on the given key factory.
     * 
     * @require type.isBasedOn(factory.getType()) : "The given type is based on the type of the factory.";
     */
    @Pure
    public static @Nonnull <C extends Concept<C, E, K>, E extends Entity<E>, K> ConceptEncodingFactory<C, E, K> get(@Nonnull @Loaded SemanticType type, @Nonnull AbstractEncodingFactory<K, E> keyFactory, @Nonnull Index<C, E, K> index) {
        return new ConceptEncodingFactory<>(type, keyFactory, index);
    }
    
    /**
     * Creates a new concept encoding factory based on the given key factory.
     * 
     * @param keyFactory the factory to encode and decode the key of the concept.
     * @param index the index that caches existing concepts and creates new ones.
     * 
     * @return a new concept encoding factory based on the given key factory.
     */
    @Pure
    public static @Nonnull <C extends Concept<C, E, K>, E extends Entity<E>, K> ConceptEncodingFactory<C, E, K> get(@Nonnull AbstractEncodingFactory<K, E> keyFactory, @Nonnull Index<C, E, K> index) {
        return new ConceptEncodingFactory<>(keyFactory.getType(), keyFactory, index);
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
