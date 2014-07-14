package ch.virtualid.module.host;

import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.module.HostModule;
import ch.virtualid.module.Module;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

/**
 * This class provides database access to the credentials of the core service.
 * 
 * A log for issued credentials is needed on the host-side.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Credentials extends HostModule {
    
    static { Module.add(new Credentials()); }
    
    @Override
    protected void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            
        }
    }
    
}
