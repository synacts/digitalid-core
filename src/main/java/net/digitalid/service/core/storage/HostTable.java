package net.digitalid.service.core.storage;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.site.Site;

/**
 * This class models a database table that can be exported and imported on {@link Host hosts}.
 * 
 * @see ClientTable
 * @see SiteTable
 */
@Immutable
public abstract class HostTable extends HostTableImplementation<HostModule> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new host table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param dumpType the dump type of the new host table.
     */
    protected HostTable(@Nonnull HostModule module, @Nonnull @Validated String name, @Nonnull @Loaded SemanticType dumpType) {
        super(module, name, dumpType);
        
        module.registerHostStorage(this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Tables –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates the database tables of this storage for the given host.
     * 
     * @param host the host for which to create the database tables of this storage.
     */
    @Locked
    @NonCommitting
    public abstract void createTables(@Nonnull Host host) throws AbortException;
    
    /**
     * Deletes the database tables of this storage for the given host.
     * 
     * @param host the host for which to delete the database tables of this storage.
     */
    @Locked
    @NonCommitting
    public void deleteTables(@Nonnull Host host) throws AbortException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + getName(host));
        } catch (@Nonnull SQLException exception) {
            throw AbortException.get(exception);
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Overrides –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Locked
    @Override
    @NonCommitting
    public final void createTables(@Nonnull Site site) throws AbortException {
        if (site instanceof Host) createTables((Host) site);
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void deleteTables(@Nonnull Site site) throws AbortException {
        if (site instanceof Host) deleteTables((Host) site);
    }
    
}
