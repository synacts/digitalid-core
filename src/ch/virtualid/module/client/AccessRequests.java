package ch.virtualid.module.client;

import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.module.ClientModule;
import ch.virtualid.module.CoreService;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

/**
 * This class provides database access to the requests of the core service.
 * 
 * TODO: For access and accreditation requests.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class AccessRequests implements ClientModule {
    
    static { CoreService.SERVICE.add(new AccessRequests()); }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
            // TODO: Create the tables of this module.
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    static { CoreService.SERVICE.add(new AccessRequests()); }
    
    @Override
    protected void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            
        }
    }
    
}
