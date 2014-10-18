package ch.virtualid.database;

import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Unit testing of the {@link Database} with the {@link SQLiteConfiguration}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class SQLiteTest extends DatabaseTest {
    
    @Override
    protected boolean isSubclass() {
        return true;
    }
    
    @BeforeClass
    public static void configureDatabase() throws SQLException {
        Database.initialize(new SQLiteConfiguration(), false, true);
        createTables();
    }
    
    @AfterClass
    public static void dropDatabase() throws SQLException {
        Database.getConfiguration().dropDatabase();
    }
    
}
