package net.digitalid.core.concept;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.concurrency.map.ConcurrentHashMapBuilder;
import net.digitalid.utility.concurrency.map.ConcurrentMap;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.mode.SingleAccess;
import net.digitalid.database.interfaces.Database;

import net.digitalid.core.entity.Entity;

/**
 * This class indexes the instances of a {@link Concept concept} by their {@link Entity entity} and key.
 */
@Immutable
@GenerateSubclass
public abstract class ConceptIndex<E extends Entity, K, C extends Concept<E, K>> {
    
    /* -------------------------------------------------- Removal -------------------------------------------------- */
    
    /**
     * Stores a list of all the indexes that were created.
     */
    private static final @Nonnull List<@Nonnull ConceptIndex<?, ?, ?>> indexes = new LinkedList<>();
    
    /**
     * Removes the entries of the given entity from all indexes.
     */
    // TODO: Make sure this method is called in the right places!
    @Pure // TODO: Should be impure!
    @SingleAccess
    public static void remove(@Nonnull Entity entity) {
        Require.that(Database.isSingleAccess()).orThrow("The database is in single-access mode.");
        
        for (@Nonnull ConceptIndex<?, ?, ?> index : indexes) {
            index.concepts.remove(entity);
        }
    }
    
    /* -------------------------------------------------- Module -------------------------------------------------- */
    
    /**
     * Returns the concept module, which contains the concept factory.
     */
    @Pure
    protected abstract @Nonnull ConceptModule<E, K, C> getConceptModule();
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected ConceptIndex() {
        indexes.add(this);
    }
    
    /* -------------------------------------------------- Concepts -------------------------------------------------- */
    
    /**
     * Stores the concepts of this index.
     */
    private final @Nonnull ConcurrentMap<@Nonnull E, @Nonnull ConcurrentMap<@Nonnull K, @Nonnull C>> concepts = ConcurrentHashMapBuilder.build();
    
    /**
     * Returns the potentially cached concept with the given entity and key that might not yet exist in the database.
     */
    @Pure
    public @Nonnull C get(@Nonnull E entity, @Nonnull K key) {
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<K, C> map = concepts.get(entity);
            if (map == null) { map = concepts.putIfAbsentElseReturnPresent(entity, ConcurrentHashMapBuilder.<K, C>build()); }
            @Nullable C concept = map.get(key);
            if (concept == null) { concept = map.putIfAbsentElseReturnPresent(key, getConceptModule().getConceptFactory().evaluate(entity, key)); }
            return concept;
        } else {
            return getConceptModule().getConceptFactory().evaluate(entity, key);
        }
    }
    
    /* -------------------------------------------------- Resetting -------------------------------------------------- */
    
    // TODO:
    
//    /**
//     * Resets the concepts of the given entity.
//     * 
//     * @param entity the entity whose concepts are to be reset.
//     * @param table the table which initiated the reset of its properties.
//     */
//    @NonCommitting
//    public void reset(@Nonnull E entity, @Nonnull ConceptPropertyTable<?, C, E> table) throws DatabaseException {
//        if (Database.isSingleAccess()) {
//            final @Nullable ConcurrentMap<K, C> map = concepts.get(entity);
//            if (map != null) {
//                for (final @Nonnull C concept : map.values()) { concept.reset(table); }
//            }
//        }
//    }
    
}
