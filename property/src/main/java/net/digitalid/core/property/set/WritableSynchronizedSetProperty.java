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
import net.digitalid.database.conversion.WhereConditionBuilder;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.property.set.PersistentSetObserver;
import net.digitalid.database.property.set.PersistentSetPropertyEntry;
import net.digitalid.database.property.set.PersistentSetPropertyEntryBuilder;
import net.digitalid.database.property.set.ReadOnlyPersistentSetProperty;
import net.digitalid.database.property.set.WritablePersistentSetPropertyImplementation;

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
@Mutable(ReadOnlyPersistentSetProperty.class)
public abstract class WritableSynchronizedSetProperty<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable VALUE, @Unspecifiable READONLY_SET extends ReadOnlySet<@Nonnull @Valid VALUE>, @Unspecifiable FREEZABLE_SET extends FreezableSet<@Nonnull @Valid VALUE>> extends WritablePersistentSetPropertyImplementation<CoreUnit, SUBJECT, VALUE, READONLY_SET, FREEZABLE_SET> implements SynchronizedProperty<ENTITY, KEY, SUBJECT, PersistentSetPropertyEntry<SUBJECT, VALUE>, PersistentSetObserver<SUBJECT, VALUE, READONLY_SET>> {
    
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
        if (get().contains(value)) {
            Database.commit();
            return false;
        } else {
            Synchronizer.execute(SetPropertyInternalActionBuilder.withProperty(this).withValue(value).withAdded(true).build());
            return true;
        }
    }
    
    @Impure
    @Override
    @Committing
    @LockNotHeldByCurrentThread
    public boolean remove(@NonCaptured @Unmodified @Nonnull @Valid VALUE value) throws DatabaseException, RecoveryException {
        if (get().contains(value)) {
            Synchronizer.execute(SetPropertyInternalActionBuilder.withProperty(this).withValue(value).withAdded(false).build());
            return true;
        } else {
            Database.commit();
            return false;
        }
    }
    
    /**
     * Adds the given value to the values of this property without synchronization.
     * This method is intended to be called only by other actions.
     * 
     * @return whether the given value was not already stored.
     */
    @Impure
    @NonCommitting
    @LockNotHeldByCurrentThread
    public boolean addWithoutSynchronization(@Captured @Nonnull @Valid VALUE value) throws DatabaseException, RecoveryException {
        if (get().contains(value)) {
            return false;
        } else {
            modify(value, true);
            return true;
        }
    }
    
    /**
     * Adds the given value to the values of this property without synchronization.
     * This method is intended to be called only by other actions.
     * 
     * @return whether the given value was not already stored.
     */
    @Impure
    @NonCommitting
    @LockNotHeldByCurrentThread
    public boolean removeWithoutSynchronization(@NonCaptured @Unmodified @Nonnull @Valid VALUE value) throws DatabaseException, RecoveryException {
        if (get().contains(value)) {
            modify(value, false);
            return true;
        } else {
            return false;
        }
    }
    
    /* -------------------------------------------------- Action -------------------------------------------------- */
    
    /**
     * Adds or removes the given value to or from this property from the {@link SetPropertyInternalAction}.
     */
    @Impure
    @NonCommitting
    @LockNotHeldByCurrentThread
    @TODO(task = "Throw a database exception if no value was deleted (because it did not exist).", date = "2017-08-17", author = Author.KASPAR_ETTER)
    protected void modify(@Nonnull @Valid VALUE value, boolean added) throws DatabaseException, RecoveryException {
        lock.lock();
        try {
            final @Nonnull PersistentSetPropertyEntry<SUBJECT, VALUE> entry = PersistentSetPropertyEntryBuilder.<SUBJECT, VALUE>withSubject(getSubject()).withValue(value).build();
            if (added) {
                SQL.insertOrAbort(getTable(), entry, getSubject().getUnit());
                getSet().add(value);
            } else {
                SQL.delete(getTable(), getSubject().getUnit(), WhereConditionBuilder.withConverter(getTable()).withObject(entry).build());
                getSet().remove(value);
            }
            notifyObservers(value, added);
        } finally {
            lock.unlock();
        }
    }
    
}
