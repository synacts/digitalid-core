package ch.virtualid.module.client;

import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.Contact;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.module.ClientModule;
import ch.virtualid.service.CoreService;
import ch.virtualid.service.Service;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

/**
 * This class provides database access to the access requests of the core service.
 * 
 * @see Contact
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class AccessRequests implements ClientModule {
    
    public static final AccessRequests MODULE = new AccessRequests();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Create the tables of this module.
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
