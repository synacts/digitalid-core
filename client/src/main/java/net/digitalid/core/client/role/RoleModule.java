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
package net.digitalid.core.client.role;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.conversion.converters.Integer64Converter;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.elements.UniqueElements;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.conversion.WhereCondition;
import net.digitalid.database.conversion.WhereConditionBuilder;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.client.Client;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.InternalPerson;
import net.digitalid.core.unit.GeneralUnit;

/**
 * This class provides database access to the {@link Role roles} of the core service.
 */
@Utility
public abstract class RoleModule {
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores a dummy configuration in order to have an initialization target for table creation.
     */
    public static final @Nonnull Configuration<Boolean> configuration = Configuration.with(Boolean.TRUE);
    
    /* -------------------------------------------------- Creation -------------------------------------------------- */
    
    /**
     * Creates the database table.
     */
    @Committing
    @PureWithSideEffects
    @Initialize(target = RoleModule.class, dependencies = {IdentifierResolver.class, GeneralUnit.class})
    public static void createTable() throws DatabaseException {
        SQL.createTable(RoleEntryConverter.INSTANCE, GeneralUnit.INSTANCE);
    }
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    /**
     * Checks whether the role with the given arguments is already mapped and returns the existing or newly mapped role.
     */
    @NonCommitting
    @PureWithSideEffects
    public static @Nonnull Role map(@Nonnull RoleArguments roleArguments) throws DatabaseException, RecoveryException {
        final @Nonnull WhereCondition<RoleArguments> roleArgumentsWhereCondition = WhereConditionBuilder.withConverter(RoleArgumentsConverter.INSTANCE).withObject(roleArguments).withPrefix("arguments").build();
        @Nullable RoleEntry roleEntry = SQL.selectFirst(RoleEntryConverter.INSTANCE, null, GeneralUnit.INSTANCE, roleArgumentsWhereCondition);
        if (roleEntry == null) {
            long key = 0; // The cache uses zero to encode a null requester.
            while (key == 0) { key = Math.abs(ThreadLocalRandom.current().nextLong()); }
            roleEntry = RoleEntryBuilder.withKey(key).withArguments(roleArguments).build();
            SQL.insertOrAbort(RoleEntryConverter.INSTANCE, roleEntry, GeneralUnit.INSTANCE);
        }
        return roleEntry.toRole();
    }
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
    /**
     * Returns the role with the given key.
     */
    @Pure
    @NonCommitting
    static @Nonnull Role load(long key) throws DatabaseException, RecoveryException {
        final @Nonnull WhereCondition<Long> whereCondition = WhereConditionBuilder.withConverter(Integer64Converter.INSTANCE).withObject(key).withPrefix("key").build();
        final @Nonnull RoleEntry roleEntry = SQL.selectOne(RoleEntryConverter.INSTANCE, null, GeneralUnit.INSTANCE, whereCondition);
        return roleEntry.toRole();
    }
    
    /* -------------------------------------------------- Removing -------------------------------------------------- */
    
    /**
     * Removes the given role, which triggers the removal of all associated core subjects.
     */
    @NonCommitting
    @PureWithSideEffects
    public static void remove(@Nonnull Role role) throws DatabaseException {
        SQL.delete(RoleEntryConverter.INSTANCE, GeneralUnit.INSTANCE, WhereConditionBuilder.withConverter(Integer64Converter.INSTANCE).withObject(role.getKey()).withPrefix("key").build());
    }
    
    /* -------------------------------------------------- Native Roles -------------------------------------------------- */
    
    /**
     * Returns the native roles of the given client.
     */
    @Pure
    @NonCommitting
    @TODO(task = "Make sure that the prefix is correct and that a null where-argument is translated to 'IS NULL' in SQL.", date = "2017-03-27", author = Author.KASPAR_ETTER)
    public static @Nonnull @UniqueElements @NonNullableElements FiniteIterable<NativeRole> getNativeRoles(@Nonnull Client client) throws DatabaseException, RecoveryException {
        final @Nonnull WhereCondition<Long> whereCondition = WhereConditionBuilder.withConverter(Integer64Converter.INSTANCE).withObject(null).withPrefix("key").build();
        final @Nonnull FreezableList<RoleEntry> entries = SQL.selectAll(RoleEntryConverter.INSTANCE, null, GeneralUnit.INSTANCE, whereCondition);
        return entries.map(RoleEntry::toRole).instanceOf(NativeRole.class).filter(role -> role.getUnit().equals(client)); // TODO: Filter the client with the where condition in the SQL select statement.
    }
    
    /* -------------------------------------------------- Non-Native Roles -------------------------------------------------- */
    
    /**
     * Returns the non-native roles of the given role.
     */
    @Pure
    @NonCommitting
    @TODO(task = "Make sure that the prefix is correct.", date = "2017-03-27", author = Author.KASPAR_ETTER)
    public static @Nonnull @UniqueElements @NonNullableElements FiniteIterable<NonNativeRole> getNonNativeRoles(@Nonnull Role role) throws DatabaseException, RecoveryException {
        final @Nonnull WhereCondition<Long> whereCondition = WhereConditionBuilder.withConverter(Integer64Converter.INSTANCE).withObject(null).withPrefix("arguments_recipient").build();
        final @Nonnull FreezableList<RoleEntry> entries = SQL.selectAll(RoleEntryConverter.INSTANCE, null, GeneralUnit.INSTANCE, whereCondition);
        return entries.map(RoleEntry::toRole).instanceOf(NonNativeRole.class);
    }
    
    /* -------------------------------------------------- Role for Person -------------------------------------------------- */
    
    /**
     * Returns the role that the given client has for the given person.
     * 
     * @throws RequestException if no such role can be found.
     */
    @Pure
    @NonCommitting
    @TODO(task = "Make sure that the prefix is correct.", date = "2017-03-27", author = Author.KASPAR_ETTER)
    static @Nonnull Role getRole(@Nonnull Client client, @Nonnull InternalPerson person) throws DatabaseException, RecoveryException, RequestException {
        final @Nonnull WhereCondition<Long> whereCondition = WhereConditionBuilder.withConverter(Integer64Converter.INSTANCE).withObject(null).withPrefix("arguments_issuer_key").build();
        final @Nullable RoleEntry entry = SQL.selectFirst(RoleEntryConverter.INSTANCE, null, GeneralUnit.INSTANCE, whereCondition); // TODO: Also filter for the right client.
        if (entry != null) { return entry.toRole(); }
        else { throw RequestExceptionBuilder.withCode(RequestErrorCode.IDENTITY).withMessage("No role for the person '" + person.getAddress() + "' could be found.").build(); }
    }
    
}
