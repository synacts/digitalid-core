package net.digitalid.core.subject;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.concurrency.map.ConcurrentHashMapBuilder;
import net.digitalid.utility.concurrency.map.ConcurrentMap;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.mode.SingleAccess;
import net.digitalid.database.interfaces.DatabaseUtility;

import net.digitalid.core.entity.Entity;

/**
 * This class indexes the instances of a {@link CoreSubject concept} by their {@link Entity entity} and key.
 */
@Immutable
@GenerateSubclass
@TODO(task = "Don't we need a SubjectIndex in the database layer?", date = "2017-01-22", author = Author.KASPAR_ETTER)
public abstract class CoreSubjectIndex<ENTITY extends Entity<?>, KEY, CONCEPT extends CoreSubject<ENTITY, KEY>> {
    
    /* -------------------------------------------------- Removal -------------------------------------------------- */
    
    /**
     * Stores a list of all the indexes that were created.
     */
    private static final @Nonnull List<@Nonnull CoreSubjectIndex<?, ?, ?>> indexes = new LinkedList<>();
    
    /**
     * Removes the entries of the given entity from all indexes.
     */
    // TODO: Make sure this method is called in the right places!
    @Impure
    @SingleAccess
    public static void remove(@Nonnull Entity<?> entity) {
        Require.that(DatabaseUtility.isSingleAccess()).orThrow("The database is in single-access mode.");
        
        for (@Nonnull CoreSubjectIndex<?, ?, ?> index : indexes) {
            index.concepts.remove(entity);
        }
    }
    
    /* -------------------------------------------------- Module -------------------------------------------------- */
    
    /**
     * Returns the concept module, which contains the concept factory.
     */
    @Pure
    protected abstract @Nonnull CoreSubjectModule<ENTITY, KEY, CONCEPT> getConceptModule();
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected CoreSubjectIndex() {
        indexes.add(this);
    }
    
    /* -------------------------------------------------- Concepts -------------------------------------------------- */
    
    /**
     * Stores the concepts of this index.
     */
    private final @Nonnull ConcurrentMap<@Nonnull ENTITY, @Nonnull ConcurrentMap<@Nonnull KEY, @Nonnull CONCEPT>> concepts = ConcurrentHashMapBuilder.build();
    
    /**
     * Returns the potentially cached concept with the given entity and key that might not yet exist in the database.
     */
    @Pure
    public @Nonnull CONCEPT get(@Nonnull ENTITY entity, @Nonnull KEY key) {
        if (DatabaseUtility.isSingleAccess()) {
            @Nullable ConcurrentMap<KEY, CONCEPT> map = concepts.get(entity);
            if (map == null) { map = concepts.putIfAbsentElseReturnPresent(entity, ConcurrentHashMapBuilder.<KEY, CONCEPT>build()); }
            @Nullable CONCEPT concept = map.get(key);
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
