package net.digitalid.core.property.value;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Specifiable;
import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.Captured;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.lock.LockNotHeldByCurrentThread;
import net.digitalid.utility.validation.annotations.type.Mutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.value.PersistentValueObserver;
import net.digitalid.database.property.value.PersistentValuePropertyEntry;
import net.digitalid.database.property.value.PersistentValuePropertyEntryBuilder;
import net.digitalid.database.property.value.ReadOnlyPersistentValueProperty;
import net.digitalid.database.property.value.WritablePersistentValuePropertyImplementation;

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
@Mutable(ReadOnlyPersistentValueProperty.class)
public abstract class WritableSynchronizedValueProperty<@Unspecifiable ENTITY extends Entity<?>, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Specifiable VALUE> extends WritablePersistentValuePropertyImplementation<CoreUnit, SUBJECT, VALUE> implements SynchronizedProperty<ENTITY, KEY, SUBJECT, PersistentValuePropertyEntry<SUBJECT, VALUE>, PersistentValueObserver<SUBJECT, VALUE>> {
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull SynchronizedValuePropertyTable<ENTITY, KEY, SUBJECT, VALUE, ?> getTable();
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    @Impure
    @Override
    @Committing
    @LockNotHeldByCurrentThread
    public @Capturable @Valid VALUE set(@Captured @Valid VALUE newValue) throws DatabaseException, RecoveryException {
        lock.lock();
        try {
            if (!loaded) { load(false); }
            final @Valid VALUE oldValue = value;
            if (!Objects.equals(newValue, oldValue)) {
                final @Nullable Time oldTime = time;
                final @Nonnull Time newTime = TimeBuilder.build();
                Synchronizer.execute(ValuePropertyInternalActionBuilder.withProperty(this).withOldValue(oldValue).withNewValue(newValue).withOldTime(oldTime).withNewTime(newTime).build());
            }
            return oldValue;
        } finally {
            lock.unlock();
        }
    }
    
    /* -------------------------------------------------- Action -------------------------------------------------- */
    
    /**
     * Replaces the time and value of this property from the {@link ValuePropertyInternalAction}.
     * 
     * @require !Objects.equals(newValue, oldValue) : "The new value may not be the same as the old value.";
     */
    @Impure
    @NonCommitting
    @LockNotHeldByCurrentThread
    protected void replace(@Nullable Time oldTime, @Nullable Time newTime, @Valid VALUE oldValue, @Valid VALUE newValue) throws DatabaseException, RecoveryException {
        lock.getReentrantLock().lock();
        try {
            final @Nonnull PersistentValuePropertyEntry<SUBJECT, VALUE> entry = PersistentValuePropertyEntryBuilder.<SUBJECT, VALUE>withSubject(getSubject()).withTime(newTime).withValue(newValue).build();
            SQL.insertOrReplace(getTable().getEntryConverter(), entry, getSubject().getUnit());
            this.time = newTime;
            this.value = newValue;
            notifyObservers(oldValue, newValue);
        } finally {
            lock.unlock();
        }
    }
    
}
