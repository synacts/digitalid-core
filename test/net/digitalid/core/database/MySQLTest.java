package net.digitalid.core.database;

import net.digitalid.database.configuration.MySQLConfiguration;
import net.digitalid.database.configuration.Database;
import java.io.IOException;
import java.sql.SQLException;
import net.digitalid.database.annotations.Committing;
import net.digitalid.annotations.state.Pure;
import org.junit.BeforeClass;

/**
 * Unit testing of the {@link Database} with the {@link MySQLConfiguration}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class MySQLTest extends DatabaseTest {
    
    @Pure
    @Override
    protected boolean isSubclass() {
        return true;
    }
    
    @BeforeClass
    @Committing
    public static void configureDatabase() throws SQLException, IOException {
        Database.initialize(new MySQLConfiguration(true), false);
        createTables();
    }
    
}
