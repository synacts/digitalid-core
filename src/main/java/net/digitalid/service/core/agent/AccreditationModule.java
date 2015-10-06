package net.digitalid.core.agent;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.annotations.state.Pure;
import net.digitalid.annotations.state.Stateless;
import net.digitalid.database.configuration.Database;
import net.digitalid.core.entity.Site;
import net.digitalid.core.data.ClientModule;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.data.Service;

/**
 * This class provides database access to the accreditation requests of the core service.
 * 
 * @see ClientAgent
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
@Stateless
public final class AccreditationModule implements ClientModule {
    
    /**
     * Stores an instance of this module.
     */
    public static final AccreditationModule MODULE = new AccreditationModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Create the tables of this module.
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
