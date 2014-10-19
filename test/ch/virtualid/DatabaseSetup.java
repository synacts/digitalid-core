package ch.virtualid;

import ch.virtualid.database.Database;
import ch.virtualid.database.MySQLConfiguration;
import ch.virtualid.database.PostgreSQLConfiguration;
import ch.virtualid.database.SQLiteConfiguration;
import ch.virtualid.identity.SemanticType;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Sets up the {@link Database} for testing.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class DatabaseSetup {
    
    @BeforeClass
    public static void configureDatabase() throws SQLException, IOException, ClassNotFoundException {
        final int configuration = 0;
        switch (configuration) {
            case 0: Database.initialize(new MySQLConfiguration(), false, true); break;
            case 1: Database.initialize(new PostgreSQLConfiguration(), false, true); break;
            case 2: Database.initialize(new SQLiteConfiguration(), false, true); break;
            default: throw new SQLException("No such configuration available.");
        }
        Class.forName(SemanticType.class.getName());
    }
    
    @AfterClass
    public static void dropDatabase() throws SQLException {
        Database.getConfiguration().dropDatabase();
    }
    
}
