package net.digitalid.core.concept;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.attribute.Attribute;
import net.digitalid.core.collections.ConcurrentHashMap;
import net.digitalid.core.collections.ConcurrentMap;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Entity;

/**
 * This class indexes the instances of a concept by their entity and key.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Index<C extends Concept<C>, K> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concepts –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the concepts of this index.
     */
    private final @Nonnull ConcurrentMap<Entity, ConcurrentMap<K, C>> concepts = ConcurrentHashMap.get();
    
    /**
     * 
     * 
     * @param entity
     * @param key
     * @param concept
     * @return 
     */
    public @Nonnull C get(@Nonnull Entity entity, @Nonnull K key, @Nonnull C concept) {
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<K, C> map = concepts.get(entity);
            if (map == null) map = concepts.putIfAbsentElseReturnPresent(entity, ConcurrentHashMap.<K, C>get());
            @Nullable C concept = map.get(key);
            if (concept == null) concept = map.putIfAbsentElseReturnPresent(key, new Attribute(entity, type));
            return concept;
        } else {
            return concept;
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Removal –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    // TODO: Keep an index of all indexes so that all values can removed if an entity is removed.
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Entity.DELETED);
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    private Index() {
        super(16, 0.75f, 16);
        
        
    }
    
    @Pure
    public static @Nonnull <C extends Concept<C>, K> Index<C, K> get() {
        return new Index<>();
    }
    
}
