package net.digitalid.core.property.map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.Captured;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.collections.map.FreezableMap;
import net.digitalid.utility.collections.map.ReadOnlyMap;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.lock.LockNotHeldByCurrentThread;
import net.digitalid.utility.validation.annotations.type.Mutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.map.PersistentMapObserver;
import net.digitalid.database.property.map.PersistentMapPropertyEntry;
import net.digitalid.database.property.map.PersistentMapPropertyEntryBuilder;
import net.digitalid.database.property.map.ReadOnlyPersistentMapProperty;
import net.digitalid.database.property.map.WritablePersistentMapPropertyImplementation;

import net.digitalid.core.entity.CoreUnit;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.SynchronizedProperty;
import net.digitalid.core.subject.CoreSubject;
import net.digitalid.core.synchronizer.Synchronizer;

/**
 * This synchronized property synchronizes a value across sites.
 */
@ThreadSafe
@GenerateBuilder
@GenerateSubclass
@Mutable(ReadOnlyPersistentMapProperty.class)
public abstract class WritableSynchronizedMapProperty<@Unspecifiable ENTITY extends Entity<?>, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable MAP_KEY, @Unspecifiable MAP_VALUE, @Unspecifiable READONLY_MAP extends ReadOnlyMap<@Nonnull @Valid("key") MAP_KEY, @Nonnull @Valid MAP_VALUE>, @Unspecifiable FREEZABLE_MAP extends FreezableMap<@Nonnull @Valid("key") MAP_KEY, @Nonnull @Valid MAP_VALUE>> extends WritablePersistentMapPropertyImplementation<CoreUnit, SUBJECT, MAP_KEY, MAP_VALUE, READONLY_MAP, FREEZABLE_MAP> implements SynchronizedProperty<ENTITY, KEY, SUBJECT, PersistentMapPropertyEntry<SUBJECT, MAP_KEY, MAP_VALUE>, PersistentMapObserver<SUBJECT, MAP_KEY, MAP_VALUE, READONLY_MAP>> {
    
    /* -------------------------------------------------- Map -------------------------------------------------- */
    
    /**
     * This method has to be overridden like this because otherwise the equals method in the generated subclass cannot access the map of the other property.
     */
    @Pure
    @Override
    protected abstract @Nonnull @NonFrozen FREEZABLE_MAP getMap();
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull SynchronizedMapPropertyTable<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE, ?, ?> getTable();
    
    /* -------------------------------------------------- Operations -------------------------------------------------- */
    
    @Impure
    @Override
    @Committing
    @LockNotHeldByCurrentThread
    public boolean add(@Captured @Nonnull @Valid("key") MAP_KEY key, @Captured @Nonnull @Valid MAP_VALUE value) throws DatabaseException, RecoveryException {
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
    @LockNotHeldByCurrentThread
    public @Capturable @Nullable @Valid MAP_VALUE remove(@NonCaptured @Unmodified @Nonnull @Valid("key") MAP_KEY key) throws DatabaseException, RecoveryException {
        lock.lock();
        try {
            if (!loaded) { load(false); }
            if (getMap().containsKey(key)) {
                final @Nullable @Valid MAP_VALUE value = getMap().get(key);
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
     * Adds or removes the given value to or from this property from the {@link MapPropertyInternalAction}.
     */
    @Impure
    @NonCommitting
    @LockNotHeldByCurrentThread
    protected void modify(@Nonnull @Valid("key") MAP_KEY key, @Nonnull @Valid MAP_VALUE value, boolean added) throws DatabaseException, RecoveryException {
        lock.getReentrantLock().lock();
        try {
            final @Nonnull PersistentMapPropertyEntry<SUBJECT, MAP_KEY, MAP_VALUE> entry = PersistentMapPropertyEntryBuilder.<SUBJECT, MAP_KEY, MAP_VALUE>withSubject(getSubject()).withKey(key).withValue(value).build();
            if (added) {
                SQL.insert(entry, getTable().getEntryConverter(), getSubject().getUnit());
                getMap().put(key, value);
            } else {
                SQL.delete(getTable().getEntryConverter(), entry, getTable().getEntryConverter(), getSubject().getUnit());
                getMap().remove(key);
            }
            notifyObservers(key, value, added);
        } finally {
            lock.unlock();
        }
    }
    
}
