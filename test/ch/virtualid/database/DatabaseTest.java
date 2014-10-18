package ch.virtualid.database;

import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Unit testing of the class {@link Database}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabaseTest {
    
    protected boolean isSubclass() {
        return false;
    }
    
    protected static void createTables() throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS test_identity (identity " + Database.getConfiguration().PRIMARY_KEY() + ", category " + Database.getConfiguration().TINYINT() + " NOT NULL, address VARCHAR(100) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS test_identifier (identifier VARCHAR(100) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", identity BIGINT NOT NULL, value BIGINT, PRIMARY KEY (identifier), FOREIGN KEY (identity) REFERENCES test_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS test_block (block " + Database.getConfiguration().BLOB() + " NOT NULL)");
            Database.getConnection().commit();
        }
    }
    
    @After
    public void commit() throws SQLException {
        if (isSubclass()) Database.getConnection().commit();
    }
    
    @Test
    public void _01_testKeyInsertWithStatement() throws SQLException {
        if (isSubclass()) {
            try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
                Assert.assertEquals(Database.getConfiguration().executeInsert(statement, "INSERT INTO test_identity (category, address) VALUES (1, 'a@syntacts.com')"), 1L);
            }
        }
    }
    
    @Test
    public void _02_testKeyInsertWithPreparedStatement() throws SQLException {
        if (isSubclass()) {
            final @Nonnull String SQL = "INSERT INTO test_identity (category, address) VALUES (?, ?)";
            try (@Nonnull PreparedStatement preparedStatement = Database.getConnection().prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setByte(1, (byte) 2);
                preparedStatement.setString(2, "b@syntacts.com");
                preparedStatement.executeUpdate();
                Assert.assertEquals(Database.getConfiguration().getGeneratedKey(preparedStatement), 2L);
            }
        }
    }
    
    @Test
    public void _03_testInsertWithForeignKeyConstraing() throws SQLException {
        if (isSubclass()) {
            try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
                statement.executeUpdate("INSERT INTO test_identifier (identifier, identity, value) VALUES ('a@syntacts.com', 1, 3)");
            }
        }
    }
    
    @Test
    public void _04_testLocalRollbackWithSavepoint() throws SQLException {
        if (isSubclass()) {
            try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
                statement.executeUpdate("INSERT INTO test_identifier (identifier, identity, value) VALUES ('b@syntacts.com', 2, 4)");
                final @Nullable Savepoint savepoint = Database.getConfiguration().setSavepoint();
                try {
                    statement.executeUpdate("INSERT INTO test_identifier (identifier, identity, value) VALUES ('a@syntacts.com', 1, 5)");
                    Assert.fail("An SQLException should have been thrown because a duplicate key was inserted.");
                } catch (SQLException exception) {
                    Database.getConfiguration().rollback(savepoint);
                }
                final @Nonnull ResultSet resultSet = statement.executeQuery("SELECT identity FROM test_identifier WHERE identifier = 'b@syntacts.com'");
                Assert.assertTrue(resultSet.next());
                Assert.assertEquals(resultSet.getLong(1), 2L);
            }
        }
    }
    
    @Ignore
    public void testDatabase() throws SQLException, InvalidEncodingException {
        final @Nonnull Connection connection = Database.getConnection();
        
//            Database.getConfiguration().onInsertUpdate(statement, "test_identifier", 1, "identifier", "identity", "value");
        
        // Insert or ignore an existing entry into the database (which is thus ignored).
        @Nonnull String SQL = "INSERT" + Database.getConfiguration().IGNORE() + " INTO test_identifier (identifier, identity) VALUES ('a@syntacts.com', 1)";
        try (@Nonnull Statement statement = connection.createStatement()) {
            Database.getConfiguration().onInsertNotUpdate(statement, "test_identifier");
            Database.getConfiguration().onInsertIgnore(statement, "test_identifier", "identifier");
            connection.commit();
            statement.executeUpdate(SQL);
            connection.commit();
        }
        
        // Retrieve the category of an identifier with a simple join.
        SQL = "SELECT test_identity.category FROM test_identifier JOIN test_identity ON test_identifier.identity = test_identity.identity WHERE identifier = 'a@syntacts.com'";
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) Assert.assertEquals(1L, resultSet.getLong(1));
            else Assert.fail("Entry not found in database.");
        }
        
        // Execute parallel queries on the same connection.
        SQL = "SELECT identity FROM test_identity WHERE address = ";
        try (@Nonnull Statement statement1 = connection.createStatement(); @Nonnull Statement statement2 = connection.createStatement(); @Nonnull ResultSet resultSet1 = statement1.executeQuery(SQL + "'a@syntacts.com'"); @Nonnull ResultSet resultSet2 = statement2.executeQuery(SQL + "'prepared.ch'")) {
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
                
                statement2.executeUpdate("UPDATE test_identity SET category = " + 2 * category2 + " WHERE address = 'a@syntacts.com'");
                connection2.commit();
            }
            
            try (@Nonnull Statement statement1 = connection1.createStatement(); @Nonnull ResultSet resultSet1 = statement1.executeQuery("SELECT category FROM test_identity WHERE address = 'a@syntacts.com'")) {
                long category1 = 0;
                if (resultSet1.next()) category1 = resultSet1.getLong(1);
                Assert.assertEquals(4L, category1);
                
                statement1.executeUpdate("UPDATE test_identity SET category = " + 3 * category1 + " WHERE address = 'prepared.ch'");
                connection1.commit();
            }
            
            try (@Nonnull Statement statement2 = connection2.createStatement(); @Nonnull ResultSet resultSet2 = statement2.executeQuery("SELECT identifier FROM test_identifier WHERE identity = 1")) {
                @Nonnull String identifier = "";
                if (resultSet2.next()) identifier = resultSet2.getString(1);
                Assert.assertEquals("a@syntacts.com", identifier);
                
                statement2.executeUpdate("UPDATE test_identifier SET identifier = '" + identifier + identifier + "' WHERE identity = 1");
                connection2.commit();
            }
            
        }
        
        @Nonnull String string1 = "Hello";
        @Nonnull String string2 = "World";
        
        // Insert a block with a prepared statement.
        SQL = "INSERT INTO test_block (block) VALUES (?)";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {
            @Nonnull Blockable[] elements = new Blockable[] { new StringWrapper(null, string1), new StringWrapper(null, string2) };
            @Nonnull Block block = new TupleWrapper(null, elements).toBlock();
            block.set(preparedStatement, 1);
            preparedStatement.executeUpdate();
            connection.commit();
        }
        
        // Insert a block with a prepared statement.
        SQL = "SELECT block FROM test_block";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {
            @Nonnull ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                @Nonnull Block block = Block.get(SemanticType.UNKNOWN, resultSet, 1);
                @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
                Assert.assertEquals(string1, new StringWrapper(elements.getNotNull(0)).getString());
                Assert.assertEquals(string2, new StringWrapper(elements.getNotNull(1)).getString());
            } else {
                Assert.fail("Block not found.");
            }
            connection.commit();
        }
    }
    
}
