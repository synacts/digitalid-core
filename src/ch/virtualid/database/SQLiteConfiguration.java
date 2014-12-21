package ch.virtualid.database;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.io.Directory;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;

/**
 * This class configures a SQLite database.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class SQLiteConfiguration extends Configuration implements Immutable {
    
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
     * @require Database.isValid(name) : "The name is valid for a database.";
     */
    public SQLiteConfiguration(@Nonnull String name, boolean reset) throws SQLException {
        super(new JDBC());
        
        assert Database.isValid(name) : "The name is valid for a database.";
        
        this.name = name;
        if (reset) dropDatabase();
        new SQLiteConfig().setSharedCache(true);
    }
    
    @Override
    public void dropDatabase() {
        new File(Directory.DATA.getPath() + Directory.SEPARATOR + name + ".db").delete();
    }
    
    /**
     * Creates a new SQLite configuration for the database with the given name.
     * 
     * @param reset whether the database is to be dropped first before creating it again.
     */
    public SQLiteConfiguration(boolean reset) throws SQLException {
        this("SQLite", reset);
    }
    
    /**
     * Returns whether a SQLite database exists.
     * 
     * @return whether a SQLite database exists.
     */
    public static boolean exists() {
        return new File(Directory.DATA.getPath() + Directory.SEPARATOR + "SQLite.db").exists();
    }
    
    
    @Pure
    @Override
    protected @Nonnull String getURL() {
        return "jdbc:sqlite:" + Directory.DATA.getPath() + Directory.SEPARATOR + name + ".db";
    }
    
    @Pure
    @Override
    protected @Nonnull Properties getProperties() {
        return properties;
    }
    
    
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
    public boolean supportsBinaryStream() {
        return false;
    }
    
    
    @Override
    long executeInsert(@Nonnull Statement statement, @Nonnull String SQL) throws SQLException {
        statement.executeUpdate(SQL);
        try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT last_insert_rowid()")) {
            if (resultSet.next()) return resultSet.getLong(1);
            else throw new SQLException("The given SQL statement did not generate any keys.");
        }
    }
    
}
