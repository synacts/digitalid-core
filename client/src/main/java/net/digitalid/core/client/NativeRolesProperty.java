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
package net.digitalid.core.client;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.set.FreezableHashSetBuilder;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.functional.interfaces.Predicate;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.property.set.WritableSetPropertyImplementation;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.lock.LockNotHeldByCurrentThread;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.set.WritablePersistentSimpleSetProperty;

import net.digitalid.core.client.role.NativeRole;
import net.digitalid.core.client.role.RoleModule;

/**
 * This class implements the {@link WritablePersistentSimpleSetProperty} for the native roles of a {@link Client}.
 */
@ThreadSafe
@GenerateSubclass
public abstract class NativeRolesProperty extends WritableSetPropertyImplementation<NativeRole, ReadOnlySet<NativeRole>, DatabaseException, RecoveryException, NativeRolesObserver, NativeRolesProperty> {
    
    /* -------------------------------------------------- Client -------------------------------------------------- */
    
    /**
     * Returns the client to which this property belongs.
     */
    @Pure
    public abstract @Nonnull Client getClient();
    
    /* -------------------------------------------------- Roles -------------------------------------------------- */
    
    private @Nonnull @NonFrozen @NonNullableElements FreezableSet<NativeRole> roles = FreezableHashSetBuilder.build();
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
    protected boolean loaded = false;
    
    /**
     * Loads the roles of this property from the database.
     * 
     * @param locking whether this method acquires the non-reentrant lock.
     */
    @Pure
    @NonCommitting
    protected void load(final boolean locking) throws DatabaseException, RecoveryException {
        if (locking) { lock.lock(); }
        try {
            roles.clear();
            roles.addAll(RoleModule.getNativeRoles(getClient()));
            this.loaded = true;
        } finally {
            if (locking) { lock.unlock(); }
        }
    }
    
    /* -------------------------------------------------- Getter -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    @TODO(task = "Do we need to handle the case when Access.mode.get() == Mode.MULTI differently?", date = "2017-07-22", author = Author.KASPAR_ETTER)
    public @Nonnull @NonFrozen @NonNullableElements ReadOnlySet<NativeRole> get() throws DatabaseException, RecoveryException {
        if (!loaded) { load(true); } // This should never trigger a reentrance exception as add(value), remove(value) and reset() that call external code ensure that the set is loaded.
        return roles;
    }
    
    /* -------------------------------------------------- Operations -------------------------------------------------- */
    
    @Impure
    @Override
    @LockNotHeldByCurrentThread
    public boolean add(@Nonnull NativeRole role) throws DatabaseException, RecoveryException {
        Require.that(role.getUnit() == getClient()).orThrow("The role $ does not belong to the client $.", role, getClient());
        
        lock.lock();
        try {
            if (!loaded) { load(false); }
            if (roles.contains(role)) {
                return false;
            } else {
                // The role has to have been mapped by the RoleModule.
                roles.add(role);
                notifyObservers(role, true);
                return true;
            }
        } finally {
            lock.unlock();
        }
    }
    
    @Impure
    @Override
    @LockNotHeldByCurrentThread
    public boolean remove(@Nonnull NativeRole role) throws DatabaseException, RecoveryException {
        lock.lock();
        try {
            if (!loaded) { load(false); }
            if (roles.contains(role)) {
                RoleModule.remove(role);
                roles.remove(role);
                notifyObservers(role, false);
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }
    
    /* -------------------------------------------------- Reset -------------------------------------------------- */
    
    /**
     * Resets the values of this property so that they have to be reloaded from the database on the next retrieval.
     * If the state of the database changed in the meantime, then this method is impure.
     * However, read-only properties must be able to expose this method as well.
     */
    @Pure
    @NonCommitting
    @LockNotHeldByCurrentThread
    public void reset() throws DatabaseException, RecoveryException {
        lock.lock();
        try {
            if (loaded) {
                if (observers.isEmpty()) {
                    this.loaded = false;
                } else {
                    final @Nonnull FreezableSet<NativeRole> oldSet = roles.clone();
                    load(false);
                    final @Nonnull FreezableSet<NativeRole> newSet = roles;
                    for (@Nonnull NativeRole role : newSet.exclude(oldSet)) {
                        notifyObservers(role, true);
                    }
                    for (@Nonnull NativeRole role : oldSet.exclude(newSet)) {
                        notifyObservers(role, false);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    /* -------------------------------------------------- Validator -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Predicate<? super NativeRole> getValueValidator() {
        return role -> true;
    }
    
}
