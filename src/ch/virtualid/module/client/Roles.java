package ch.virtualid.module.client;

import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.identity.Mapper;
import ch.virtualid.module.ClientModule;
import ch.virtualid.module.Module;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

/**
 * This class provides database access to the roles of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Roles extends ClientModule {
    
    static { Module.add(new Roles()); }
    
    @Override
    protected void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS role (role " + Database.getConfiguration().PRIMARY_KEY() + ", issuer BIGINT NOT NULL, relation BIGINT, recipient BIGINT, FOREIGN KEY (issuer) REFERENCES map_identity (identity), FOREIGN KEY (relation) REFERENCES map_identity (identity), FOREIGN KEY (recipient) REFERENCES map_identity (identity))");
            // TODO: Add the corresponding authorization ID? -> Yes, but now agent (ID).
            // -> the recipient should be another role, or not? -> I think so.
        }
        
        Mapper.addReference("role", "issuer");
        Mapper.addReference("role", "recipient");
    }
    
}
