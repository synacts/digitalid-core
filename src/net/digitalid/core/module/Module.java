package net.digitalid.core.module;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.Site;
import net.digitalid.core.service.Service;

/**
 * A module manages an {@link Entity entity}'s partial state in the {@link Database database}.
 * 
 * @see BothModule
 * @see HostModule
 * @see ClientModule
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
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
