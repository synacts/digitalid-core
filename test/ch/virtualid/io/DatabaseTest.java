package ch.virtualid.io;

import ch.virtualid.database.Database;
import ch.virtualid.database.PostgreSQLConfiguration;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Database}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class DatabaseTest {
    
    private void executeQueries() throws SQLException, InvalidEncodingException {
        @Nonnull Connection connection = Database.getConnection();
        
        // Create the tables if they do not yet exist.
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS test_identifier");
            statement.executeUpdate("DROP TABLE IF EXISTS test_identity");
            statement.executeUpdate("DROP TABLE IF EXISTS test_blob");
            connection.commit();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS test_identity (identity " + Database.getConfiguration().PRIMARY_KEY() + ", category " + Database.getConfiguration().TINYINT() + " NOT NULL, address VARCHAR(100) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS test_identifier (identifier VARCHAR(100) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", identity BIGINT NOT NULL, value BIGINT, PRIMARY KEY (identifier), FOREIGN KEY (identity) REFERENCES test_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS test_blob (block " + Database.getConfiguration().BLOB() + " NOT NULL)");
//            Database.getConfiguration().onInsertUpdate(statement, "test_identifier", 1, "identifier", "identity", "value");
            connection.commit();
        }
        
        // Insert an entry with an ordinary statement and retrieve the generated key.
        try (@Nonnull Statement statement = connection.createStatement()) {
            long key = Database.getConfiguration().executeInsert(statement, "INSERT INTO test_identity (category, address) VALUES (1, 'test.ch')");
            statement.executeUpdate("INSERT INTO test_identifier (identifier, identity, value) VALUES ('test.ch', " + key + ", 1)");
            final @Nullable Savepoint savepoint = Database.getConfiguration().setSavepoint();
            try {
                statement.executeUpdate("INSERT INTO test_identifier (identifier, identity, value) VALUES ('test.ch', " + key + ", 2)");
                Assert.fail("An SQLException should have been thrown because a duplicate key was inserted.");
            } catch (SQLException exception) {
                Database.getConfiguration().rollback(savepoint);
            }
            connection.commit();
        }
        
        // Insert an entry with a prepared statement and retrieve the generated key.
        @Nonnull String SQL = "INSERT INTO test_identity (category, address) VALUES (?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setByte(1, (byte) 2);
            preparedStatement.setString(2, "prepared.ch");
            preparedStatement.executeUpdate();
            Assert.assertEquals(2L, Database.getConfiguration().getGeneratedKey(preparedStatement));
            connection.commit();
        }
        
        // Insert or ignore an existing entry into the database (which is thus ignored).
        SQL = "INSERT" + Database.getConfiguration().IGNORE() + " INTO test_identifier (identifier, identity) VALUES ('test.ch', 1)";
        try (@Nonnull Statement statement = connection.createStatement()) {
            Database.getConfiguration().onInsertNotUpdate(statement, "test_identifier");
            Database.getConfiguration().onInsertIgnore(statement, "test_identifier", "identifier");
            connection.commit();
            statement.executeUpdate(SQL);
            connection.commit();
        }
        
        // Retrieve the category of an identifier with a simple join.
        SQL = "SELECT test_identity.category FROM test_identifier JOIN test_identity ON test_identifier.identity = test_identity.identity WHERE identifier = 'test.ch'";
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) Assert.assertEquals(1L, resultSet.getLong(1));
            else Assert.fail("Entry not found in database.");
        }
        
        // Execute parallel queries on the same connection.
        SQL = "SELECT identity FROM test_identity WHERE address = ";
        try (@Nonnull Statement statement1 = connection.createStatement(); @Nonnull Statement statement2 = connection.createStatement(); @Nonnull ResultSet resultSet1 = statement1.executeQuery(SQL + "'test.ch'"); @Nonnull ResultSet resultSet2 = statement2.executeQuery(SQL + "'prepared.ch'")) {
            if (resultSet1.next()) Assert.assertEquals(1L, resultSet1.getLong(1));
            else Assert.fail("Entry not found with the first statement.");
            if (resultSet2.next()) Assert.assertEquals(2L, resultSet2.getLong(1));
            else Assert.fail("Entry not found with the second statement.");
        }
        
        // Execute parallel updates on separate and committed connections.
        try (@Nonnull Connection connection2 = Database.getConnection(); @Nonnull Connection connection1 = Database.getConnection()) {
            
            try (@Nonnull Statement statement2 = connection2.createStatement(); @Nonnull ResultSet resultSet2 = statement2.executeQuery("SELECT category FROM test_identity WHERE address = 'prepared.ch'")) {
                long category2 = 0;
                if (resultSet2.next()) category2 = resultSet2.getLong(1);
                Assert.assertEquals(2L, category2);
                
                statement2.executeUpdate("UPDATE test_identity SET category = " + 2 * category2 + " WHERE address = 'test.ch'");
                connection2.commit();
            }
            
            try (@Nonnull Statement statement1 = connection1.createStatement(); @Nonnull ResultSet resultSet1 = statement1.executeQuery("SELECT category FROM test_identity WHERE address = 'test.ch'")) {
                long category1 = 0;
                if (resultSet1.next()) category1 = resultSet1.getLong(1);
                Assert.assertEquals(4L, category1);
                
                statement1.executeUpdate("UPDATE test_identity SET category = " + 3 * category1 + " WHERE address = 'prepared.ch'");
                connection1.commit();
            }
            
            try (@Nonnull Statement statement2 = connection2.createStatement(); @Nonnull ResultSet resultSet2 = statement2.executeQuery("SELECT identifier FROM test_identifier WHERE identity = 1")) {
                @Nonnull String identifier = "";
                if (resultSet2.next()) identifier = resultSet2.getString(1);
                Assert.assertEquals("test.ch", identifier);
                
                statement2.executeUpdate("UPDATE test_identifier SET identifier = '" + identifier + identifier + "' WHERE identity = 1");
                connection2.commit();
            }
            
        }
        
        /*
        @Nonnull String string1 = "Hello";
        @Nonnull String string2 = "World";
        
        // Insert a block with a prepared statement.
        SQL = "INSERT INTO test_blob (block) VALUES (?)";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {
            @Nonnull Blockable[] elements = new Blockable[] { new StringWrapper(string1), new StringWrapper(string2) };
            @Nonnull Block block = new TupleWrapper(elements).toBlock();
            Database.setBlock(preparedStatement, 1, block);
            preparedStatement.executeUpdate();
            connection.commit();
        }
        
        // Insert a block with a prepared statement.
        SQL = "SELECT block FROM test_blob";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {
            @Nonnull ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                @Nonnull Block block = Database.getBlock(resultSet, 1);
                @Nonnull Block[] elements = new TupleWrapper(block).getElementsNotNull(2);
                Assert.assertEquals(string1, new StringWrapper(elements[0]).getString());
                Assert.assertEquals(string2, new StringWrapper(elements[1]).getString());
            } else {
                Assert.fail("Block not found.");
            }
            connection.commit();
        }
        */
    }
    
    @Test
    public void testDatabase() throws SQLException, InvalidEncodingException, IOException {
        main(new String[0]);
        executeQueries();
    }
    
    public static void main(String[] args) throws SQLException, IOException {
//        Database.initialize(new MySQLConfiguration(), false, true);
        Database.initialize(new PostgreSQLConfiguration(), false, true);
//        Database.initialize(new SQLiteConfiguration(), true, true);
    }
    
}
