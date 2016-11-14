package net.digitalid.core.property.map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.Captured;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.collections.map.FreezableMap;
import net.digitalid.utility.collections.map.ReadOnlyMap;
import net.digitalid.utility.concurrency.exceptions.ReentranceException;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.map.PersistentMapPropertyEntry;
import net.digitalid.database.property.map.PersistentMapPropertyEntryBuilder;
import net.digitalid.database.property.map.ReadOnlyPersistentMapProperty;
import net.digitalid.database.property.map.WritablePersistentMapProperty;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.SynchronizedProperty;
import net.digitalid.core.synchronizer.Synchronizer;

/**
 * This synchronized property synchronizes a value across sites.
 */
@ThreadSafe
@GenerateBuilder
@GenerateSubclass
@Mutable(ReadOnlyPersistentMapProperty.class)
public abstract class WritableSynchronizedMapProperty<E extends Entity, K, C extends Concept<E, K>, U, V, R extends ReadOnlyMap<@Nonnull @Valid("key") U, @Nonnull @Valid V>, F extends FreezableMap<@Nonnull @Valid("key") U, @Nonnull @Valid V>> extends WritablePersistentMapProperty<C, U, V, R, F> implements SynchronizedProperty<E, K, C, PersistentMapPropertyEntry<C, U, V>, ReadOnlyPersistentMapProperty.Observer<C, U, V, R>> {
    
    /* -------------------------------------------------- Map -------------------------------------------------- */
    
    /**
     * This method has to be overridden like this because otherwise the equals method in the generated subclass cannot access the map of the other property.
     */
    @Pure
    @Override
    protected abstract @Nonnull @NonFrozen F getMap();
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull SynchronizedMapPropertyTable<E, K, C, U, V, ?, ?> getTable();
    
    /* -------------------------------------------------- Operations -------------------------------------------------- */
    
    @Impure
    @Override
    @Committing
    public boolean add(@Captured @Nonnull @Valid("key") U key, @Captured @Nonnull @Valid V value) throws DatabaseException, ReentranceException {
        lock.lock();
        try {
            if (!loaded) { load(false); }
            if (getMap().containsKey(key)) {
                return false;
            } else {
                Synchronizer.execute(MapPropertyInternalActionBuilder.withProperty(this).withKey(key).withValue(value).withAdded(true).build());
                return true;
            }
        } finally {
            lock.unlock();
        }
    }
    
    @Impure
    @Override
    @Committing
    public @Capturable @Nullable @Valid V remove(@NonCaptured @Unmodified @Nonnull @Valid("key") U key) throws DatabaseException, ReentranceException {
        lock.lock();
        try {
            if (!loaded) { load(false); }
            if (getMap().containsKey(key)) {
                final @Nullable @Valid V value = getMap().get(key);
                Synchronizer.execute(MapPropertyInternalActionBuilder.withProperty(this).withKey(key).withValue(value).withAdded(false).build());
                return value;
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }
    
    /* -------------------------------------------------- Action -------------------------------------------------- */
    
    /**
     * Adds or removes the given value to or from this property from the {@link SetPropertyInternalAction}.
     */
    @Impure
    @NonCommitting
    @TODO(task = "Implement and use SQL.delete().", date = "2016-11-12", author = Author.KASPAR_ETTER, assignee = Author.STEPHANIE_STROKA, priority = Priority.HIGH)
    protected void modify(@Nonnull @Valid("key") U key, @Nonnull @Valid V value, boolean added) throws DatabaseException {
        lock.getReentrantLock().lock();
        try {
            final @Nonnull PersistentMapPropertyEntry<C, U, V> entry = PersistentMapPropertyEntryBuilder.<C, U, V>withSubject(getSubject()).withKey(key).withValue(value).build();
            if (added) {
                SQL.insert(entry, getTable().getEntryConverter(), getSubject().getSite());
                getMap().put(key, value);
            } else {
                // TODO (of course without SQL injection!): SQL.delete(getTable().getEntryConverter(), SQLBooleanAlias.with("key = " + key), getSubject().getSite());
                getMap().remove(key);
            }
            notifyObservers(key, value, true);
        } finally {
            lock.unlock();
        }
    }
    
}
