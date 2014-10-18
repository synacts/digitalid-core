package ch.virtualid.database;

import java.io.IOException;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Unit testing of the {@link Database} with the {@link PostgreSQLConfiguration}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class PostgreSQLTest extends DatabaseTest {
    
    @Override
    protected boolean isSubclass() {
        return true;
    }
    
    @BeforeClass
    public static void configureDatabase() throws SQLException, IOException {
        Database.initialize(new PostgreSQLConfiguration(), false, true);
        createTables();
    }
    
    @AfterClass
    public static void dropDatabase() throws SQLException {
        Database.getConfiguration().dropDatabase();
    }
    
}
