package ch.virtualid.module;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Site;
import ch.virtualid.service.Service;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * A module manages an {@link Entity entity}'s partial state in the {@link Database database}.
 * 
 * @see BothModule
 * @see HostModule
 * @see ClientModule
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface Module {
    
    /**
     * Returns the service to which this module belongs.
     * 
     * @return the service to which this module belongs.
     */
    @Pure
    public @Nonnull Service getService();
    
    /**
     * Creates the database tables for the given site.
     * 
     * @param site the site for which to create the database tables.
     */
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException;
    
    /**
     * Deletes the database tables for the given site.
     * 
     * @param site the site for which to delete the database tables.
     */
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException;
    
}
