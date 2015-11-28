package net.digitalid.service.core.database;

import java.io.IOException;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.configuration.Database;
import net.digitalid.database.core.configuration.PostgreSQLConfiguration;
import net.digitalid.utility.annotations.state.Pure;
import org.junit.BeforeClass;

/**
 * Unit testing of the {@link Database} with the {@link PostgreSQLConfiguration}.
 */
public final class PostgreSQLTest extends DatabaseTest {
    
    @Pure
    @Override
    protected boolean isSubclass() {
        return true;
    }
    
    @BeforeClass
    @Committing
    public static void configureDatabase() throws DatabaseException, IOException {
        Database.initialize(new PostgreSQLConfiguration(true), false);
        createTables();
    }
    
}
