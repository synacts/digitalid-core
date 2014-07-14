package ch.virtualid.module.host;

import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.module.HostModule;
import ch.virtualid.module.Module;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

/**
 * This class provides database access to the tokens of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Tokens extends HostModule {
    
    static { Module.add(new Tokens()); }
    
    @Override
    protected void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS public (host BIGINT NOT NULL, public BOOLEAN NOT NULL, PRIMARY KEY (host), FOREIGN KEY (host) REFERENCES map_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS token (host BIGINT NOT NULL, token CHAR(19) BIGINT NOT NULL, PRIMARY KEY (host, token), FOREIGN KEY (host) REFERENCES map_identity (identity))");
        }
    }
    
}
