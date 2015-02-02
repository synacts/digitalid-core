package ch.virtualid.database;

import ch.virtualid.annotations.EndsCommitted;
import ch.virtualid.annotations.Pure;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.BeforeClass;

/**
 * Unit testing of the {@link Database} with the {@link PostgreSQLConfiguration}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class PostgreSQLTest extends DatabaseTest {
    
    @Pure
    @Override
    protected boolean isSubclass() {
        return true;
    }
    
    @BeforeClass
    @EndsCommitted
    public static void configureDatabase() throws SQLException, IOException {
        Database.initialize(new PostgreSQLConfiguration(true), false, true);
        createTables();
    }
    
}
