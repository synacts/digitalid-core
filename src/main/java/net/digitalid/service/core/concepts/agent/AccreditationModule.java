package net.digitalid.service.core.concepts.agent;

import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.configuration.Database;
import net.digitalid.database.core.site.Site;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.service.core.storage.ClientModule;
import net.digitalid.service.core.storage.Service;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;

/**
 * This class provides database access to the accreditation requests of the core service.
 * 
 * @see ClientAgent
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
    public void createTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Create the tables of this module.
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
