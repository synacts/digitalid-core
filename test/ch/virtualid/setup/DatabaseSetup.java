package ch.virtualid.setup;

import ch.virtualid.database.Database;
import ch.virtualid.database.MySQLConfiguration;
import ch.virtualid.database.PostgreSQLConfiguration;
import ch.virtualid.database.SQLiteConfiguration;
import ch.virtualid.identity.SemanticType;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up the {@link Database} for testing.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class DatabaseSetup {
    
    @BeforeClass
    public static void setupDatabase() throws SQLException, IOException, ClassNotFoundException {
        final int configuration = 0;
        switch (configuration) {
            case 0: Database.initialize(new MySQLConfiguration(), false, false); break;
            case 1: Database.initialize(new PostgreSQLConfiguration(), false, false); break;
            case 2: Database.initialize(new SQLiteConfiguration(), false, false); break;
            default: throw new SQLException("No such configuration available.");
        }
        Class.forName(SemanticType.class.getName());
    }
    
    @AfterClass
    public static void breakDownDatabase() throws SQLException {
        Database.getConfiguration().dropDatabase();
    }
    
    @Test
    public final void testDatabaseSetup() throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery("SELECT 1")) {
            Assert.assertTrue(resultSet.next());
            Assert.assertEquals(1, resultSet.getInt(1));
        }
    }
    
}
