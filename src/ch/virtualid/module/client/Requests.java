package ch.virtualid.module.client;

import ch.virtualid.database.ClientEntity;
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
public final class Requests {
    
    /**
     * Initializes the database by creating the appropriate tables if necessary.
     * 
     * @param connection an open client connection to the database.
     */
    public static void initialize(@Nonnull ClientEntity connection) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            
        }
    }
    
}
