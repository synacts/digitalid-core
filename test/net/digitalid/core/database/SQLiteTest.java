package net.digitalid.core.database;

import java.sql.SQLException;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Pure;
import org.junit.BeforeClass;

/**
 * Unit testing of the {@link Database} with the {@link SQLiteConfiguration}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class SQLiteTest extends DatabaseTest {
    
    @Pure
    @Override
    protected boolean isSubclass() {
        return true;
    }
    
    @BeforeClass
    @Committing
    public static void configureDatabase() throws SQLException {
        Database.initialize(new SQLiteConfiguration(true), false);
        createTables();
    }
    
}
