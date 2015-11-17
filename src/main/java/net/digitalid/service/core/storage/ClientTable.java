package net.digitalid.service.core.storage;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.site.Site;

/**
 * This class models a database table that can be created and deleted on {@link Client clients}.
 * 
 * @see HostTable
 * @see SiteTable
 */
@Immutable
public abstract class ClientTable extends ClientTableImplementation<ClientModule> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new client table with the given module and name.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     */
    protected ClientTable(@Nonnull ClientModule module, @Nonnull @Validated String name) {
        super(module, name);
        
        module.registerClientStorage(this);
    }
    
    /* -------------------------------------------------- Tables -------------------------------------------------- */
    
    /**
     * Creates the database tables of this storage for the given client.
     * 
     * @param client the client for which to create the database tables of this storage.
     */
    @Locked
    @NonCommitting
    public abstract void createTables(@Nonnull Client client) throws AbortException;
    
    /**
     * Deletes the database tables of this storage for the given client.
     * 
     * @param client the client for which to delete the database tables of this storage.
     */
    @Locked
    @NonCommitting
    public void deleteTables(@Nonnull Client client) throws AbortException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + getName(client));
        } catch (@Nonnull SQLException exception) {
            throw AbortException.get(exception);
        }
    }
    
    /* -------------------------------------------------- Overrides -------------------------------------------------- */
    
    @Locked
    @Override
    @NonCommitting
    public final void createTables(@Nonnull Site site) throws AbortException {
        if (site instanceof Client) { createTables((Client) site); }
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void deleteTables(@Nonnull Site site) throws AbortException {
        if (site instanceof Client) { deleteTables((Client) site); }
    }
    
}
