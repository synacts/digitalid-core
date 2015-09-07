package net.digitalid.core.module;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.Site;
import net.digitalid.core.service.Service;

/**
 * A module stores a partial state of an {@link Entity entity} in the {@link Database database}.
 * 
 * @see HostModule
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class Module<T extends Table<T>> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Service –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the service to which this module belongs.
     */
    private final @Nonnull Service service;
    
    /**
     * Returns the service to which this module belongs.
     * 
     * @return the service to which this module belongs.
     */
    @Pure
    public @Nonnull Service getService() {
        return service;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new module with the given service.
     * 
     * @param service the service to which the new module belongs.
     */
    protected Module(@Nonnull Service service) {
        this.service = service;
    }
    
    /**
     * Returns a new module with the given service.
     * 
     * @param service the service to which the new module belongs.
     * 
     * @return a new module with the given service.
     */
    @Pure
    public static @Nonnull <T extends Table<T>> Module<T> get(@Nonnull Service service) {
        return new Module<>(service);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Tables –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the tables of this module.
     */
    private final @Nonnull @NonNullableElements @NonFrozen FreezableList<T> tables = FreezableLinkedList.get();
    
    /**
     * Registers the given table at this module.
     * 
     * @param table the table to be registered.
     */
    public final void register(@Nonnull T table) {
        tables.add(table);
    }
    
    /**
     * Returns the tables of this module.
     * 
     * @return the tables of this module.
     */
    @Pure
    public final @Nonnull @NonNullableElements ReadOnlyList<T> getTables() {
        return tables;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Creation and Deletion –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates the database tables for the given site.
     * 
     * @param site the site for which to create the database tables.
     */
    @Locked
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException {
        for (final @Nonnull Table<T> table : tables) table.create(site);
    }
    
    /**
     * Deletes the database tables for the given site.
     * 
     * @param site the site for which to delete the database tables.
     */
    @Locked
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException {
        for (final @Nonnull Table<T> table : tables) table.delete(site);
    }
    
}
