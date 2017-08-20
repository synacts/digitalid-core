package net.digitalid.core.subject;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.concurrency.map.ConcurrentHashMapBuilder;
import net.digitalid.utility.concurrency.map.ConcurrentMap;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.access.Access;
import net.digitalid.database.access.Mode;
import net.digitalid.database.access.annotations.SingleAccess;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.Entity;

/**
 * This class indexes the instances of a {@link CoreSubject core subject} by their {@link Entity entity} and key.
 */
@Immutable
@GenerateSubclass
@TODO(task = "Make the subjects somehow observable and expose a read-only view through the subject class? Problem: The index only knows the loaded subjects, not all that exist.", date = "2017-08-20", author = Author.KASPAR_ETTER)
public abstract class CoreSubjectIndex<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>> {
    
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
    public static void remove(@Nonnull Entity entity) {
        Require.that(Access.mode.get() == Mode.SINGLE).orThrow("The database has to be in single-access mode.");
        
        for (@Nonnull CoreSubjectIndex<?, ?, ?> index : indexes) {
            index.subjects.remove(entity);
        }
    }
    
    /* -------------------------------------------------- Module -------------------------------------------------- */
    
    /**
     * Returns the core subject module, which contains the core subject factory.
     */
    @Pure
    protected abstract @Nonnull CoreSubjectModule<ENTITY, KEY, SUBJECT> getSubjectModule();
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected CoreSubjectIndex() {
        indexes.add(this);
    }
    
    /* -------------------------------------------------- Subjects -------------------------------------------------- */
    
    /**
     * Stores the core subjects of this index.
     */
    private final @Nonnull ConcurrentMap<@Nonnull ENTITY, @Nonnull ConcurrentMap<@Nonnull KEY, @Nonnull SUBJECT>> subjects = ConcurrentHashMapBuilder.build();
    
    /**
     * Returns the potentially cached core subject with the given entity and key after having inserted it into its database table.
     */
    @Pure
    public @Nonnull SUBJECT get(@Nonnull ENTITY entity, @Nonnull KEY key) throws DatabaseException {
        @Nullable SUBJECT subject;
        if (Access.mode.get() == Mode.SINGLE) {
            @Nullable ConcurrentMap<KEY, SUBJECT> map = subjects.get(entity);
            if (map == null) { map = subjects.putIfAbsentElseReturnPresent(entity, ConcurrentHashMapBuilder.<KEY, SUBJECT>build()); }
            subject = map.get(key);
            if (subject == null) { subject = map.putIfAbsentElseReturnPresent(key, getSubjectModule().getSubjectFactory().evaluate(entity, key)); }
        } else {
            subject = getSubjectModule().getSubjectFactory().evaluate(entity, key);
        }
        SQL.insertOrReplace(getSubjectModule().getSubjectTable(), subject, entity.getUnit()); // SQL.insertOrIgnore would be more suitable but is not supported by H2 and all columns are the primary key anyway.
        return subject;
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
