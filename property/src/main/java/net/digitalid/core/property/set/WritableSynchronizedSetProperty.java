package net.digitalid.core.property.set;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Captured;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.collections.set.ReadOnlySet;
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
import net.digitalid.database.property.set.PersistentSetObserver;
import net.digitalid.database.property.set.PersistentSetPropertyEntry;
import net.digitalid.database.property.set.PersistentSetPropertyEntryBuilder;
import net.digitalid.database.property.set.ReadOnlyPersistentSetProperty;
import net.digitalid.database.property.set.WritablePersistentSetPropertyImplementation;

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
@Mutable(ReadOnlyPersistentSetProperty.class)
public abstract class WritableSynchronizedSetProperty<@Unspecifiable ENTITY extends Entity<?>, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable VALUE, @Unspecifiable READONLY_SET extends ReadOnlySet<@Nonnull @Valid VALUE>, @Unspecifiable FREEZABLE_SET extends FreezableSet<@Nonnull @Valid VALUE>> extends WritablePersistentSetPropertyImplementation<CoreUnit, SUBJECT, VALUE, READONLY_SET, FREEZABLE_SET> implements SynchronizedProperty<ENTITY, KEY, SUBJECT, PersistentSetPropertyEntry<SUBJECT, VALUE>, PersistentSetObserver<SUBJECT, VALUE, READONLY_SET>> {
    
    /* -------------------------------------------------- Set -------------------------------------------------- */
    
    /**
     * This method has to be overridden like this because otherwise the equals method in the generated subclass cannot access the set of the other property.
     */
    @Pure
    @Override
    protected abstract @Nonnull @NonFrozen FREEZABLE_SET getSet();
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull SynchronizedSetPropertyTable<ENTITY, KEY, SUBJECT, VALUE, ?> getTable();
    
    /* -------------------------------------------------- Operations -------------------------------------------------- */
    
    @Impure
    @Override
    @Committing
    @LockNotHeldByCurrentThread
    public boolean add(@Captured @Nonnull @Valid VALUE value) throws DatabaseException, RecoveryException {
        lock.lock();
        try {
            if (!loaded) { load(false); }
            if (getSet().contains(value)) {
                return false;
            } else {
                Synchronizer.execute(SetPropertyInternalActionBuilder.withProperty(this).withValue(value).withAdded(true).build());
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
    public boolean remove(@NonCaptured @Unmodified @Nonnull @Valid VALUE value) throws DatabaseException, RecoveryException {
        lock.lock();
        try {
            if (!loaded) { load(false); }
            if (getSet().contains(value)) {
                Synchronizer.execute(SetPropertyInternalActionBuilder.withProperty(this).withValue(value).withAdded(false).build());
                return true;
            } else {
                return false;
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
    @LockNotHeldByCurrentThread
    @TODO(task = "Implement and use SQL.delete().", date = "2016-11-12", author = Author.KASPAR_ETTER, assignee = Author.STEPHANIE_STROKA, priority = Priority.HIGH)
    protected void modify(@Nonnull @Valid VALUE value, boolean added) throws DatabaseException, RecoveryException {
        lock.getReentrantLock().lock();
        try {
            final @Nonnull PersistentSetPropertyEntry<SUBJECT, VALUE> entry = PersistentSetPropertyEntryBuilder.<SUBJECT, VALUE>withSubject(getSubject()).withValue(value).build();
            if (added) {
                SQL.insert(getTable().getEntryConverter(), entry, getSubject().getUnit());
                getSet().add(value);
            } else {
                SQL.delete(getTable().getEntryConverter(), entry, getTable().getEntryConverter(), getSubject().getUnit());
                getSet().remove(value);
            }
            notifyObservers(value, added);
        } finally {
            lock.unlock();
        }
    }
    
}
