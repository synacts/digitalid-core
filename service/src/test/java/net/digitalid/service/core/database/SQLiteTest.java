package net.digitalid.service.core.database;

import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.configuration.SQLiteConfiguration;

import org.junit.BeforeClass;

/**
 * Unit testing of the {@link Database} with the {@link SQLiteConfiguration}.
 */
public final class SQLiteTest extends DatabaseTest {
    
    @Pure
    @Override
    protected boolean isSubclass() {
        return true;
    }
    
    @BeforeClass
    @Committing
    public static void configureDatabase() throws DatabaseException {
        Database.initialize(new SQLiteConfiguration(true), false);
        createTables();
    }
    
}
