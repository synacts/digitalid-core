package ch.virtualid.module.client;

import ch.virtualid.database.ClientEntity;
import ch.virtualid.database.Database;
import ch.virtualid.identity.Mapper;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

/**
 * This class provides database access to the roles of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Roles {
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "BIGINT";
    
    /**
     * Stores the foreign key constraint used to reference instances of this class.
     */
    public static final @Nonnull String REFERENCE = "REFERENCES role (role) ON DELETE CASCADE ON UPDATE CASCADE";
    
    /**
     * Initializes the database by creating the appropriate tables if necessary.
     * 
     * @param connection an open client connection to the database.
     */
    public static void initialize(@Nonnull ClientEntity connection) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS role (role " + Database.PRIMARY_KEY + ", issuer BIGINT NOT NULL, relation BIGINT, recipient BIGINT, FOREIGN KEY (issuer) REFERENCES map_identity (identity), FOREIGN KEY (relation) REFERENCES map_identity (identity), FOREIGN KEY (recipient) REFERENCES map_identity (identity))");
            // TODO: Add the corresponding authorization ID? -> Yes, but now agent (ID).
            // -> the recipient should be another role, or not? -> I think so.
        }
        
        Mapper.addReference("role", "issuer");
        Mapper.addReference("role", "recipient");
    }
    
}
