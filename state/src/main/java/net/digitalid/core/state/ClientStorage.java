package net.digitalid.core.state;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;

import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.table.Site;

/**
 * This interface models a storage that can be created and deleted on {@link Client clients} and {@link Host hosts}.
 * 
 * @see HostStorage
 * @see ClientTableImplementation
 * @see DelegatingClientStorageImplementation
 */
public interface ClientStorage {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the service to which this storage belongs.
     * 
     * @return the service to which this storage belongs.
     */
    @Pure
    public @Nonnull Service getService();
    
    /**
     * Returns the name of this storage.
     * 
     * @return the name of this storage.
     */
    @Pure
    public @Nonnull @Validated String getName();
    
    /**
     * Returns the name of this storage with the prefix of the given site.
     * 
     * @param site the site whose prefix is to be used for the returned name.
     * 
     * @return the name of this storage with the prefix of the given site.
     */
    @Pure
    public @Nonnull @Validated String getName(@Nonnull Site site);
    
    /* -------------------------------------------------- Tables -------------------------------------------------- */
    
    /**
     * Creates the database tables of this storage for the given site.
     * 
     * @param site the site for which to create the database tables of this storage.
     */
    @Locked
    @NonCommitting
    public void createTables(@Nonnull Site site) throws DatabaseException;
    
    /**
     * Deletes the database tables of this storage for the given site.
     * 
     * @param site the site for which to delete the database tables of this storage.
     */
    @Locked
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws DatabaseException;
    
}
