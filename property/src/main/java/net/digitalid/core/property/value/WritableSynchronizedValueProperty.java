/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.property.value;

import java.sql.SQLException;
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
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.lock.LockNotHeldByCurrentThread;
import net.digitalid.utility.validation.annotations.type.Mutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.exceptions.DatabaseExceptionBuilder;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.property.value.PersistentValueObserver;
import net.digitalid.database.property.value.PersistentValuePropertyEntry;
import net.digitalid.database.property.value.PersistentValuePropertyEntryBuilder;
import net.digitalid.database.property.value.ReadOnlyPersistentValueProperty;
import net.digitalid.database.property.value.WritablePersistentValuePropertyImplementation;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.SynchronizedProperty;
import net.digitalid.core.subject.CoreSubject;
import net.digitalid.core.synchronizer.Synchronizer;
import net.digitalid.core.unit.CoreUnit;

/**
 * This synchronized property synchronizes a value across sites.
 */
@ThreadSafe
@GenerateBuilder
@GenerateSubclass
@Mutable(ReadOnlyPersistentValueProperty.class)
public abstract class WritableSynchronizedValueProperty<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Specifiable VALUE> extends WritablePersistentValuePropertyImplementation<CoreUnit, SUBJECT, VALUE> implements SynchronizedProperty<ENTITY, KEY, SUBJECT, PersistentValuePropertyEntry<SUBJECT, VALUE>, PersistentValueObserver<SUBJECT, VALUE>> {
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull SynchronizedValuePropertyTable<ENTITY, KEY, SUBJECT, VALUE, ?> getTable();
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    @Impure
    @Committing
    @LockNotHeldByCurrentThread
    protected @Capturable @Valid VALUE set(@Captured @Valid VALUE newValue, final boolean synchronization) throws DatabaseException, RecoveryException {
        final @Nonnull Pair<@Valid VALUE, @Nullable Time> valueWithTimeOfLastModification = getValueWithTimeOfLastModification();
        final @Valid VALUE oldValue = valueWithTimeOfLastModification.get0();
        if (!Objects.equals(newValue, oldValue)) {
            final @Nonnull Time newTime = TimeBuilder.build();
            final @Nullable Time oldTime = valueWithTimeOfLastModification.get1();
            if (synchronization) { Synchronizer.execute(ValuePropertyInternalActionBuilder.withProperty(this).withOldValue(oldValue).withNewValue(newValue).withOldTime(oldTime).withNewTime(newTime).build()); }
            else { replace(oldTime, newTime, oldValue, newValue); }
        } else if (synchronization) { Database.commit(); }
        return oldValue;
    }
    
    @Impure
    @Override
    @Committing
    @LockNotHeldByCurrentThread
    public @Capturable @Valid VALUE set(@Captured @Valid VALUE newValue) throws DatabaseException, RecoveryException {
        return set(newValue, true);
    }
    
    /**
     * Sets the value of this property to the given value without synchronization.
     * This method is intended to be called only by other actions.
     * 
     * @return the old value of this property that got replaced by the given value.
     */
    @Impure
    @NonCommitting
    @LockNotHeldByCurrentThread
    public @Capturable @Valid VALUE setWithoutSynchronization(@Captured @Valid VALUE newValue) throws DatabaseException, RecoveryException {
        return set(newValue, false);
    }
    
    /* -------------------------------------------------- Action -------------------------------------------------- */
    
    /**
     * Replaces the time and value of this property from the {@link ValuePropertyInternalAction}.
     * 
     * @require !Objects.equals(newValue, oldValue) : "The new value is not the same as the old value.";
     */
    @Impure
    @NonCommitting
    @LockNotHeldByCurrentThread
    protected void replace(@Nullable Time oldTime, @Nullable Time newTime, @Valid VALUE oldValue, @Valid VALUE newValue) throws DatabaseException, RecoveryException {
        Require.that(!Objects.equals(newValue, oldValue)).orThrow("The new value $ may not be the same as the old value $.", newValue, oldValue);
        
        lock.lock();
        try {
            if (!loaded) { load(false); }
            if (!Objects.equals(oldValue, value)) { throw DatabaseExceptionBuilder.withCause(new SQLException(Strings.format("Could not replace the old value $ because the current value $ was different.", oldValue, value))).build(); }
            final @Nonnull PersistentValuePropertyEntry<SUBJECT, VALUE> entry = PersistentValuePropertyEntryBuilder.<SUBJECT, VALUE>withSubject(getSubject()).withTime(newTime).withValue(newValue).build();
            SQL.insertOrReplace(getTable(), entry, getSubject().getUnit());
            this.time = newTime;
            this.value = newValue;
            this.loaded = true;
            notifyObservers(oldValue, newValue);
        } finally {
            lock.unlock();
        }
    }
    
}
