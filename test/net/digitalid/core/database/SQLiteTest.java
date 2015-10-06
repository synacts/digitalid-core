package net.digitalid.core.database;

import net.digitalid.database.configuration.Database;
import net.digitalid.database.configuration.SQLiteConfiguration;
import java.sql.SQLException;
import net.digitalid.database.annotations.Committing;
import net.digitalid.annotations.state.Pure;
import org.junit.BeforeClass;

/**
 * Unit testing of the {@link Database} with the {@link SQLiteConfiguration}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
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
