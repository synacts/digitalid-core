package net.digitalid.core.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.io.Directory;
import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;

/**
 * This class configures a SQLite database.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class SQLiteConfiguration extends Configuration {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Existence –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns whether a SQLite database exists.
     * 
     * @return whether a SQLite database exists.
     */
    @Pure
    public static boolean exists() {
        return new File(Directory.getDataDirectory().getPath() + File.separator + "SQLite.db").exists();
    }
    
    /**
     * Returns whether a SQLite journal exists.
     * 
     * @return whether a SQLite journal exists.
     */
    @Pure
    public boolean journalExists() {
        return new File(Directory.getDataDirectory().getPath() + File.separator + name + ".db-journal").exists();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the name of the database.
     */
    private final @Nonnull String name;
    
    /**
     * Stores the user and the password as properties.
     */
    private final @Nonnull Properties properties = new Properties();
    
    /**
     * Creates a new SQLite configuration for the database with the given name.
     * 
     * @param name the name of the database file (without the suffix).
     * @param reset whether the database is to be dropped first before creating it again.
     * 
     * @require Configuration.isValid(name) : "The name is valid for a database.";
     */
    @Committing
    public SQLiteConfiguration(@Nonnull @Validated String name, boolean reset) throws SQLException {
        super(new JDBC());
        
        assert Configuration.isValid(name) : "The name is valid for a database.";
        
        this.name = name;
        if (reset) {
            Database.lock();
            dropDatabase();
            Database.unlock();
        }
        new SQLiteConfig().setSharedCache(true);
    }
    
    /**
     * Creates a new SQLite configuration for the database with the given name.
     * 
     * @param name the name of the database file (without the suffix).
     * 
     * @require Configuration.isValid(name) : "The name is valid for a database.";
     */
    @Committing
    public SQLiteConfiguration(@Nonnull @Validated String name) throws SQLException {
        this(name, false);
    }
    
    /**
     * Creates a new SQLite configuration for the database with the default name.
     * 
     * @param reset whether the database is to be dropped first before creating it again.
     */
    @Committing
    public SQLiteConfiguration(boolean reset) throws SQLException {
        this("SQLite", reset);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Database –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    protected @Nonnull String getURL() {
        return "jdbc:sqlite:" + Directory.getDataDirectory().getPath() + File.separator + name + ".db";
    }
    
    @Pure
    @Override
    protected @Nonnull Properties getProperties() {
        return properties;
    }
    
    @Pure
    @Override
    protected @Nonnull Connection getConnection() throws SQLException {
        final @Nonnull Connection connection = DriverManager.getConnection(getURL(), getProperties());
        connection.setAutoCommit(false);
        return connection;
    }
    
    @Locked
    @Override
    public void dropDatabase() {
        assert Database.isLocked() : "The database is locked.";
        
        new File(Directory.getDataDirectory().getPath() + File.separator + name + ".db").delete();
        new File(Directory.getDataDirectory().getPath() + File.separator + name + ".db-journal").delete();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Syntax –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull String PRIMARY_KEY() {
        return "INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT";
    }
    
    @Pure
    @Override
    public @Nonnull String TINYINT() {
        return "TINYINT";
    }
    
    @Pure
    @Override
    public @Nonnull String BINARY() {
        return "BINARY";
    }
    
    @Pure
    @Override
    public @Nonnull String NOCASE() {
        return "NOCASE";
    }
    
    @Pure
    @Override
    public @Nonnull String CITEXT() {
        return "TEXT";
    }
    
    @Pure
    @Override
    public @Nonnull String BLOB() {
        return "BLOB";
    }
    
    @Pure
    @Override
    public @Nonnull String HASH() {
        return "BLOB";
    }
    
    @Pure
    @Override
    public @Nonnull String VECTOR() {
        return "BLOB";
    }
    
    @Pure
    @Override
    public @Nonnull String REPLACE() {
        return "REPLACE";
    }
    
    @Pure
    @Override
    public @Nonnull String IGNORE() {
        return " OR IGNORE";
    }
    
    @Pure
    @Override
    public @Nonnull String GREATEST() {
        return "MAX";
    }
    
    @Pure
    @Override
    public @Nonnull String CURRENT_TIME() {
        return "CAST((JULIANDAY('NOW') - 2440587.5)*86400000 AS INTEGER)";
    }
    
    @Pure
    @Override
    public @Nonnull String BOOLEAN(boolean value) {
        return value ? "1" : "0";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Index –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull String INDEX(@Nonnull String... columns) {
        assert columns.length > 0 : "The length of the columns is positive.";
        
        return "";
    }
    
    @Pure
    @Locked
    @Override
    @SuppressWarnings("StringEquality")
    public void createIndex(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) throws SQLException {
        assert columns.length > 0 : "The length of the columns is positive.";
        
        final @Nonnull StringBuilder string = new StringBuilder("CREATE INDEX IF NOT EXISTS ").append(table).append("_index ON ").append(table).append(" (");
        for (final @Nonnull String column : columns) {
            if (column != columns[0]) string.append(", ");
            string.append(column);
        }
        statement.executeUpdate(string.append(")").toString());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Supports –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean supportsBinaryStream() {
        return false;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Insertions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Locked
    @Override
    @NonCommitting
    protected long executeInsert(@Nonnull Statement statement, @Nonnull String SQL) throws SQLException {
        statement.executeUpdate(SQL);
        try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT last_insert_rowid()")) {
            if (resultSet.next()) return resultSet.getLong(1);
            else throw new SQLException("The given SQL statement did not generate any keys.");
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Locking –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores a reentrant lock to serialize database access.
     */
    private final @Nonnull ReentrantLock lock = new ReentrantLock(true);
    
    @Override
    protected void lock() {
        lock.lock();
    }
    
    @Override
    protected void unlock() {
        lock.unlock();
    }
    
    @Pure
    @Override
    protected boolean isLocked() {
        return lock.isHeldByCurrentThread();
    }
    
}
