package net.digitalid.core.state;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.exceptions.UnexpectedValueException;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.table.Site;

/**
 * This class implements a storage that delegates the creation and deletion to substorages on {@link Host hosts} and {@link Client clients}.
 * 
 * @see ClientModule
 * @see DelegatingHostStorageImplementation
 */
@Immutable
abstract class DelegatingClientStorageImplementation implements ClientStorage {
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    /**
     * Stores the service to which this storage belongs.
     */
    private final @Nonnull Service service;
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return service;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    /**
     * Returns whether the given name is valid.
     * 
     * @param name the name to be checked.
     * 
     * @return whether the given name is valid.
     */
    @Pure
    public static boolean isValidName(@Nonnull String name) {
        return name.length() <= 22 && name.startsWith("_") && name.length() > 1 && Database.getConfiguration().isValidIdentifier(name);
    }
    
    /**
     * Stores the name of this storage.
     */
    private final @Nonnull @Validated String name;
    
    @Pure
    @Override
    public final @Nonnull @Validated String getName() {
        return name;
    }
    
    @Pure
    @Override
    public final @Nonnull @Validated String getName(@Nonnull Site site) {
        return site + name;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */

    /**
     * Creates a new client storage with the given service and name.
     * 
     * @param service the service to which the new storage belongs.
     * @param name the name of the new storage without any prefix.
     */
    protected DelegatingClientStorageImplementation(@Nullable Service service, @Nonnull @Validated String name) {
        if (this instanceof Service) {
            this.service = (Service) this;
            this.name = "_" + name;
        } else if (service != null) {
            this.service = service;
            this.name = service.getName() + "_" + name;
        } else {
            throw UnexpectedValueException.with("Only the service class should call this constructor with null.");
        }
        
        Require.that(isValidName(this.name)).orThrow("The name is valid.");
    }
    
    /* -------------------------------------------------- Substorages -------------------------------------------------- */
    
    /**
     * Stores the substorages of this storage.
     */
    private final @Nonnull @NonNullableElements @NonFrozen FreezableList<ClientStorage> substorages = FreezableLinkedList.get();
    
    /**
     * Returns the substorages of this storage.
     * 
     * @return the substorages of this storage.
     */
    @Pure
    public final @Nonnull @NonNullableElements @NonFrozen ReadOnlyList<ClientStorage> getSubstorages() {
        return substorages;
    }
    
    /**
     * Registers the given substorage at this storage.
     * 
     * @param substorage the substorage to be registered.
     */
    final void registerClientStorage(@Nonnull ClientStorage substorage) {
        substorages.add(substorage);
    }
    
    /* -------------------------------------------------- Creation and Deletion -------------------------------------------------- */
    
    @Locked
    @Override
    @NonCommitting
    public final void createTables(@Nonnull Site site) throws DatabaseException {
        for (final @Nonnull ClientStorage substorage : substorages) {
            substorage.createTables(site);
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void deleteTables(@Nonnull Site site) throws DatabaseException {
        for (final @Nonnull ClientStorage substorage : substorages) {
            substorage.deleteTables(site);
        }
    }
    
}
