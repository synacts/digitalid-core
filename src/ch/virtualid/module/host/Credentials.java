package ch.virtualid.module.host;

import ch.virtualid.database.HostEntity;
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
public final class Credentials {
    
    /**
     * Initializes the database by creating the appropriate tables if necessary.
     * 
     * @param connection an open host connection to the database.
     */
    public static void initialize(@Nonnull HostEntity connection) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            
        }
    }
    
}
