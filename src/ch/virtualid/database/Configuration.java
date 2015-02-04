package ch.virtualid.database;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Immutable;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is used to configure various databases.
 * 
 * @see MySQLConfiguration
 * @see PostgreSQLConfiguration
 * @see SQLiteConfiguration
 * 
 * @see Database
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class Configuration implements Immutable {
    
    /**
     * Creates a new configuration with the given driver.
     * 
     * @param driver the JDBC driver of this configuration.
     */
    @NonCommitting
    protected Configuration(@Nonnull Driver driver) throws SQLException {
        DriverManager.registerDriver(driver);
    }
    
    /**
     * Returns the database URL of this configuration.
     * 
     * @return the database URL of this configuration.
     */
    @Pure
    protected abstract @Nonnull String getURL();
    
    /**
     * Returns the properties of this configuration.
     * <p>
     * <em>Important:</em> Do not modify them!
     * 
     * @return the properties of this configuration.
     */
    @Pure
    protected abstract @Nonnull Properties getProperties();
    
    /**
     * Drops the configured database.
     */
    @NonCommitting
    public abstract void dropDatabase() throws SQLException;
    
    
    /**
     * Returns the syntax for defining an auto-incrementing primary key.
     * 
     * @return the syntax for defining an auto-incrementing primary key.
     */
    @Pure
    public abstract @Nonnull String PRIMARY_KEY();
    
    /**
     * Returns the syntax for defining a tiny integer.
     * 
     * @return the syntax for defining a tiny integer.
     */
    @Pure
    public abstract @Nonnull String TINYINT();
    
    /**
     * Returns the syntax for defining a binary collation.
     * 
     * @return the syntax for defining a binary collation.
     */
    @Pure
    public abstract @Nonnull String BINARY();
    
    /**
     * Returns the syntax for defining a case-insensitive collation.
     * 
     * @return the syntax for defining a case-insensitive collation.
     */
    @Pure
    public abstract @Nonnull String NOCASE();
    
    /**
     * Returns the syntax for defining a case-insensitive text.
     * 
     * @return the syntax for defining a case-insensitive text.
     */
    @Pure
    public abstract @Nonnull String CITEXT();
    
    /**
     * Returns the syntax for defining a binary large object.
     * 
     * @return the syntax for defining a binary large object.
     */
    @Pure
    public abstract @Nonnull String BLOB();
    
    /**
     * Returns the syntax for defining a 256-bit hash.
     * 
     * @return the syntax for defining a 256-bit hash.
     */
    @Pure
    public abstract @Nonnull String HASH();
    
    /**
     * Returns the syntax for defining a 128-bit vector.
     * 
     * @return the syntax for defining a 128-bit vector.
     */
    @Pure
    public abstract @Nonnull String VECTOR();
    
    /**
     * Returns the syntax for replacing existing entries during inserts.
     * 
     * @return the syntax for replacing existing entries during inserts.
     */
    @Pure
    public abstract @Nonnull String REPLACE();
    
    /**
     * Returns the syntax for ignoring database errors during updates.
     * 
     * @return the syntax for ignoring database errors during updates.
     */
    @Pure
    public abstract @Nonnull String IGNORE();
    
    /**
     * Returns the syntax for retrieving the greatest argument.
     * 
     * @return the syntax for retrieving the greatest argument.
     */
    @Pure
    public abstract @Nonnull String GREATEST();
    
    /**
     * Returns the syntax for retrieving the current time in milliseconds.
     * 
     * @return the syntax for retrieving the current time in milliseconds.
     */
    @Pure
    public abstract @Nonnull String CURRENT_TIME();
    
    
    /**
     * Returns the syntax for creating an index inside a table declaration.
     * 
     * @param columns the columns for which the index is to be created.
     * 
     * @return the syntax for creating an index inside a table declaration.
     * 
     * @require columns.length > 0 : "The length of the columns is positive.";
     */
    @Pure
    public abstract @Nonnull String INDEX(@Nonnull String... columns);
    
    /**
     * Creates an index outside a table declaration or does nothing.
     * 
     * @param statement the statement on which the creation is executed.
     * @param table the table on whose columns the index is to be created.
     * @param columns the columns for which the index is to be created.
     * 
     * @require columns.length > 0 : "The length of the columns is positive.";
     */
    public abstract void createIndex(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) throws SQLException;
    
    
    /**
     * Returns whether binary streams are supported.
     * 
     * @return the syntax for retrieving the current time in milliseconds.
     */
    @Pure
    public boolean supportsBinaryStream() {
        return true;
    }
    
    
    /**
     * Executes the given insertion and returns the generated key.
     * 
     * @param statement a statement to execute the insertion.
     * @param SQL an SQL statement that inserts an entry.
     * 
     * @return the key generated for the inserted entry.
     */
    @NonCommitting
    long executeInsert(@Nonnull Statement statement, @Nonnull String SQL) throws SQLException {
        statement.executeUpdate(SQL, Statement.RETURN_GENERATED_KEYS);
        try (@Nonnull ResultSet resultSet = statement.getGeneratedKeys()) {
            if (resultSet.next()) return resultSet.getLong(1);
            else throw new SQLException("The given SQL statement did not generate a key.");
        }
    }
    
    
    /**
     * Returns a savepoint for the given connection or null if not supported or required.
     * 
     * @param connection the connection for which a savepoint is to be returned.
     * 
     * @return a savepoint for the given connection or null if not supported or required.
     */
    @NonCommitting
    @Nullable Savepoint setSavepoint(@Nonnull Connection connection) throws SQLException {
        return null;
    }
    
    /**
     * Rolls back the given connection to the given savepoint and releases the savepoint afterwards.
     * 
     * @param connection the connection which is to be rolled back and whose savepoint is to be released.
     * @param savepoint the savepoint to roll the connection back to or null if not supported or required.
     */
    @NonCommitting
    void rollback(@Nonnull Connection connection, @Nullable Savepoint savepoint) throws SQLException {}
    
    
    /**
     * Creates a rule to ignore duplicate insertions.
     * 
     * @param statement a statement to create the rule with.
     * @param table the table to which the rule is applied.
     * @param columns the columns of the primary key.
     * 
     * @require columns.length > 0 : "At least one column is provided.";
     */
    @NonCommitting
    void onInsertIgnore(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) throws SQLException {}
    
    /**
     * Drops the rule to ignore duplicate insertions.
     * 
     * @param statement a statement to drop the rule with.
     * @param table the table from which the rule is dropped.
     */
    @NonCommitting
    void onInsertNotIgnore(@Nonnull Statement statement, @Nonnull String table) throws SQLException {}
    
    
    /**
     * Creates a rule to update duplicate insertions.
     * 
     * @param statement a statement to create the rule with.
     * @param table the table to which the rule is applied.
     * @param key the number of columns in the primary key.
     * @param columns the columns which are inserted starting with the columns of the primary key.
     * 
     * @require key > 0 : "The number of columns in the primary key is positive.";
     * @require columns.length >= key : "At least as many columns as in the primary key are provided.";
     */
    @NonCommitting
    void onInsertUpdate(@Nonnull Statement statement, @Nonnull String table, int key, @Nonnull String... columns) throws SQLException {}
    
    /**
     * Drops the rule to update duplicate insertions.
     * 
     * @param statement a statement to drop the rule with.
     * @param table the table from which the rule is dropped.
     */
    @NonCommitting
    void onInsertNotUpdate(@Nonnull Statement statement, @Nonnull String table) throws SQLException {}
    
}
