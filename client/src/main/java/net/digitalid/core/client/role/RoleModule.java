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
import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.elements.UniqueElements;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.client.Client;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.identification.identity.InternalNonHostIdentityConverter;
import net.digitalid.core.identification.identity.InternalPerson;

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
    @Initialize(target = RoleModule.class, dependencies = SQL.class) // This is not optimal yet. However, these tables have to be created before the cache table.
    public static void createTable() throws DatabaseException {
        SQL.createTable(RoleConverter.INSTANCE, Unit.DEFAULT); // TODO: Remove this line as soon as a converter can declare its foreign key constraint.
        SQL.createTable(RoleEntryConverter.INSTANCE, Unit.DEFAULT);
    }
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    /**
     * Checks whether the role with the given arguments is already mapped and returns the existing or newly mapped role.
     */
    @NonCommitting
    @PureWithSideEffects
    public static @Nonnull Role map(@Nonnull RoleArguments roleArguments) throws DatabaseException, RecoveryException {
        final @Nullable RoleEntry roleEntry = SQL.selectFirst(RoleEntryConverter.INSTANCE, roleArguments.getClient(), RoleArgumentsConverter.INSTANCE, roleArguments, "arguments", roleArguments.getClient());
        if (roleEntry == null) {
            long key = 0; // The cache uses zero to encode a null requester.
            while (key == 0) { key = ThreadLocalRandom.current().nextLong(); }
            final @Nonnull RoleEntry entry = RoleEntryBuilder.withClient(roleArguments.getClient()).withKey(key).withArguments(roleArguments).build();
            SQL.insertOrAbort(RoleEntryConverter.INSTANCE, entry, roleArguments.getClient());
            final @Nonnull Role role = entry.toRole();
            SQL.insertOrAbort(RoleConverter.INSTANCE, role, role.getUnit()); // TODO: Remove this line as soon as a converter can declare its foreign key constraint.
            return role;
        } else {
            return roleEntry.toRole();
        }
    }
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
    /**
     * Returns the role of the given client with the given key.
     */
    @Pure
    @NonCommitting
    static @Nonnull Role load(@Nonnull Client client, long key) throws DatabaseException, RecoveryException {
        final @Nonnull RoleEntry roleEntry = SQL.selectOne(RoleEntryConverter.INSTANCE, client, Integer64Converter.INSTANCE, key, "key", client);
        return roleEntry.toRole();
    }
    
    /* -------------------------------------------------- Removing -------------------------------------------------- */
    
    /**
     * Removes the given role, which triggers the removal of all associated core subjects.
     */
    @NonCommitting
    @PureWithSideEffects
    static void remove(@Nonnull Role role) throws DatabaseException {
        SQL.delete(RoleEntryConverter.INSTANCE, Integer64Converter.INSTANCE, role.getKey(), "key", role.getUnit());
        SQL.delete(RoleConverter.INSTANCE, RoleConverter.INSTANCE, role, role.getUnit()); // TODO: Remove this line as soon as a converter can declare its foreign key constraint.
    }
    
    /* -------------------------------------------------- Native Roles -------------------------------------------------- */
    
    /**
     * Returns the native roles of the given client.
     */
    @Pure
    @NonCommitting
    @TODO(task = "Make sure that the prefix is correct and that a null where-argument is translated to 'IS NULL' in SQL.", date = "2017-03-27", author = Author.KASPAR_ETTER)
    static @Nonnull @UniqueElements @NonNullableElements FiniteIterable<NativeRole> getNativeRoles(@Nonnull Client client) throws DatabaseException, RecoveryException {
        final @Nonnull FreezableList<RoleEntry> entries = SQL.selectAll(RoleEntryConverter.INSTANCE, client, Integer64Converter.INSTANCE, null, "arguments_recipient", client);
        return entries.map(RoleEntry::toRole).instanceOf(NativeRole.class);
    }
    
    /* -------------------------------------------------- Non-Native Roles -------------------------------------------------- */
    
    /**
     * Returns the non-native roles of the given role.
     */
    @Pure
    @NonCommitting
    @TODO(task = "Make sure that the prefix is correct.", date = "2017-03-27", author = Author.KASPAR_ETTER)
    static @Nonnull @UniqueElements @NonNullableElements FiniteIterable<NonNativeRole> getNonNativeRoles(@Nonnull Role role) throws DatabaseException, RecoveryException {
        final @Nonnull Client client = role.getUnit();
        final @Nonnull FreezableList<RoleEntry> entries = SQL.selectAll(RoleEntryConverter.INSTANCE, client, Integer64Converter.INSTANCE, role.getKey(), "arguments_recipient", client);
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
        final @Nullable RoleEntry entry = SQL.selectFirst(RoleEntryConverter.INSTANCE, client, InternalNonHostIdentityConverter.INSTANCE, person, "arguments_issuer", client);
        if (entry != null) { return entry.toRole(); }
        else { throw RequestExceptionBuilder.withCode(RequestErrorCode.IDENTITY).withMessage("No role for the person '" + person.getAddress() + "' could be found.").build(); }
    }
    
}
