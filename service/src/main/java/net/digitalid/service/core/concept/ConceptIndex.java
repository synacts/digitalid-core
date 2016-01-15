package net.digitalid.service.core.concept;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.annotations.SingleAccess;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.service.core.concept.property.ConceptPropertyTable;
import net.digitalid.service.core.entity.Entity;

/**
 * This class indexes the instances of a {@link Concept concept} by their {@link Entity entity} and key.
 */
@Immutable
public final class ConceptIndex<C extends Concept<C, E, K>, E extends Entity, K> {
    
    /* -------------------------------------------------- Removal -------------------------------------------------- */
    
    /**
     * Stores a list of all the indexes that were created.
     */
    private static final @Nonnull @NonNullableElements List<ConceptIndex<?, ? extends Entity, ?>> indexes = new LinkedList<>();
    
    /**
     * Removes the entries of the given entity from all indexes.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    // TODO: Make sure this method is called in the right places!
    @SingleAccess
    public static void remove(@Nonnull Entity entity) {
        assert Database.isSingleAccess() : "The database is in single-access mode.";
        
        for (final @Nonnull ConceptIndex<?, ? extends Entity, ?> index : indexes) {
            index.concepts.remove(entity);
        }
    }
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
    /**
     * Stores the factory that can produce a new concept instance with a given entity and key.
     */
    private final @Nonnull Concept.Factory<C, E, K> factory;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new index with the given concept factory.
     * 
     * @param factory the factory that can produce a new concept instance.
     */
    private ConceptIndex(@Nonnull Concept.Factory<C, E, K> factory) {
        this.factory = factory;
        indexes.add(this);
    }
    
    /**
     * Creates a new index with the given concept factory.
     * 
     * @param factory the factory that can produce a new concept instance.
     * 
     * @return a new index with the given concept factory.
     */
    @Pure
    public static @Nonnull <C extends Concept<C, E, K>, E extends Entity, K> ConceptIndex<C, E, K> get(@Nonnull Concept.Factory<C, E, K> factory) {
        return new ConceptIndex<>(factory);
    }
    
    /* -------------------------------------------------- Concepts -------------------------------------------------- */
    
    /**
     * Stores the concepts of this index.
     */
    private final @Nonnull ConcurrentMap<E, ConcurrentMap<K, C>> concepts = ConcurrentHashMap.get();
    
    /**
     * Returns a potentially cached concept that might not yet exist in the database.
     * 
     * @param entity the entity to which the concept belongs.
     * @param key the key that denotes the returned instance.
     * 
     * @return a new or existing concept with the given entity and key.
     */
    @Pure
    public @Nonnull C get(@Nonnull E entity, @Nonnull K key) {
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<K, C> map = concepts.get(entity);
            if (map == null) { map = concepts.putIfAbsentElseReturnPresent(entity, ConcurrentHashMap.<K, C>get()); }
            @Nullable C concept = map.get(key);
            if (concept == null) { concept = map.putIfAbsentElseReturnPresent(key, factory.create(entity, key)); }
            return concept;
        } else {
            return factory.create(entity, key);
        }
    }
    
    /* -------------------------------------------------- Resetting -------------------------------------------------- */
    
    /**
     * Resets the concepts of the given entity.
     * 
     * @param entity the entity whose concepts are to be reset.
     * @param table the table which initiated the reset of its properties.
     */
    @Locked
    @NonCommitting
    public void reset(@Nonnull E entity, @Nonnull ConceptPropertyTable<?, C, E> table) throws DatabaseException {
        if (Database.isSingleAccess()) {
            final @Nullable ConcurrentMap<K, C> map = concepts.get(entity);
            if (map != null) {
                for (final @Nonnull C concept : map.values()) { concept.reset(table); }
            }
        }
    }
    
}
