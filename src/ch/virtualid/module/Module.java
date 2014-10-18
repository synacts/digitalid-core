package ch.virtualid.module;

import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Site;
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
 * @version 2.0
 */
public interface Module {
    
    /**
     * Creates the database tables for the given site.
     * 
     * @param site the site for which to create the database tables.
     */
    public void createTables(@Nonnull Site site) throws SQLException;
    
    /**
     * Deletes the database tables for the given site.
     * 
     * @param site the site for which to delete the database tables.
     */
    public void deleteTables(@Nonnull Site site) throws SQLException;
    
}
