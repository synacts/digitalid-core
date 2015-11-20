package net.digitalid.service.core.setup;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.configuration.MySQLConfiguration;
import net.digitalid.utility.database.configuration.PostgreSQLConfiguration;
import net.digitalid.utility.database.configuration.SQLiteConfiguration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up the {@link Database} for testing.
 */
public class DatabaseSetup {
    
    @BeforeClass
    @Committing
    public static void setUpDatabase() throws DatabaseException, IOException, ClassNotFoundException {
        final int configuration = 0;
        switch (configuration) {
            case 0: Database.initialize(new MySQLConfiguration(true), false); break;
            case 1: Database.initialize(new PostgreSQLConfiguration(true), false); break;
            case 2: Database.initialize(new SQLiteConfiguration(true), false); break;
            default: throw new SQLException("No such configuration available.");
        }
        try {
            Database.lock();
            Class.forName(SemanticType.class.getName());
            Database.commit();
        } finally {
            Database.unlock();
        }
    }
    
    @Test
    @Committing
    public final void testDatabaseSetup() throws DatabaseException {
        if (getClass().equals(DatabaseSetup.class)) {
            try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                Assert.assertTrue(resultSet.next());
                Assert.assertEquals(1, resultSet.getInt(1));
                Database.commit();
            }
        }
    }
    
}
