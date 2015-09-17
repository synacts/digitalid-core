package net.digitalid.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Initialized;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEmpty;
import net.digitalid.core.annotations.Positive;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Stateless;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.ConcurrentHashMap;
import net.digitalid.core.collections.ConcurrentMap;
import net.digitalid.core.io.Log;
import net.digitalid.core.server.Server;
import net.digitalid.core.server.Worker;
import net.digitalid.core.storable.Storable;

/**
 * This class provides connections to the database.
 * <p>
 * <em>Important:</em> The table names without the prefix may consist of at most 22 characters!
 * Moreover, if a host is run with SQLite as database, its identifier may not contain a hyphen.
 * 
 * @see Configuration
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Stateless
public final class Database {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storing –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Sets the parameters starting from the given index of the prepared statement to the given non-nullable storable.
     * 
     * @param storable the non-nullable storable which is to be stored in the database.
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param parameterIndex the starting index of the parameters which are to be set.
     */
    public static <V extends Storable<V, ?>> void setNonNullable(@Nonnull V storable, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        storable.getFactory().setNonNullable(storable, preparedStatement, parameterIndex);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Configuration –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the configuration of the database.
     */
    private static @Nullable Configuration configuration;
    
    /**
     * Returns the configuration of the database.
     * <p>
     * <em>Important:</em> Do not store the configuration
     * permanently because it may change during testing!
     * 
     * @return the configuration of the database.
     */
    @Pure
    @Initialized
    public static @Nonnull Configuration getConfiguration() {
        assert configuration != null : "The database is initialized.";
        
        return configuration;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Single-Access –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores whether the database is set up for single-access.
     * In case of single-access, only one process accesses the
     * database, which allows to keep the objects in memory up
     * to date with no need to reload them all the time.
     * (Clients on hosts are run in multi-access mode.)
     */
    private static boolean singleAccess;
    
    /**
     * Returns whether the database is set up for single-access.
     * 
     * @return whether the database is set up for single-access.
     */
    @Pure
    public static boolean isSingleAccess() {
        return singleAccess;
    }
    
    /**
     * Returns whether the database is set up for multi-access.
     * 
     * @return whether the database is set up for multi-access.
     */
    @Pure
    public static boolean isMultiAccess() {
        return !singleAccess;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Main Thread –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores whether the current thread is the main thread used for initializations.
     */
    private static final @Nonnull ThreadLocal<Boolean> mainThread = new ThreadLocal<Boolean>() {
        @Override protected @Nonnull Boolean initialValue() {
            return false;
        }
    };
    
    /**
     * Returns whether the current thread is the main thread used for initializations.
     * 
     * @return whether the current thread is the main thread used for initializations.
     */
    @Pure
    public static boolean isMainThread() {
        return mainThread.get();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Initialization –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Initializes the database with the given configuration.
     * 
     * @param configuration the configuration of the database.
     * @param singleAccess whether the database is accessed by a single process.
     */
    public static void initialize(@Nonnull Configuration configuration, boolean singleAccess) {
        Database.configuration = configuration;
        Database.singleAccess = singleAccess;
        mainThread.set(true);
        connection.remove();
        
        Log.information("The database has been initialized for " + (singleAccess ? "single" : "multi") + "-access with a " + configuration.getClass().getSimpleName() + ".");
    }
    
    /**
     * Initializes the database with the given configuration.
     * 
     * @param configuration the configuration of the database.
     */
    public static void initialize(@Nonnull Configuration configuration) {
        initialize(configuration, true);
    }
    
    /**
     * Returns whether the database has been initialized.
     * 
     * @return whether the database has been initialized.
     */
    @Pure
    public static boolean isInitialized() { return configuration != null; }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Connection –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the open connection to the database that is associated with the current thread.
     */
    static final @Nonnull ThreadLocal<Connection> connection = new ThreadLocal<Connection>() {
        @Override protected @Nullable Connection initialValue() {
            assert configuration != null : "The database is initialized.";
            try {
                return configuration.getConnection();
            } catch (@Nonnull SQLException exception) {
                return null;
            }
        }
    };
    
    /**
     * Returns the open connection to the database that is associated with the current thread.
     * <p>
     * <em>Important:</em> Do not commit or close the connection as it will be reused later on!
     * 
     * @return the open connection to the database that is associated with the current thread.
     */
    @Pure
    @Locked
    @Initialized
    @NonCommitting
    static @Nonnull Connection getConnection() throws SQLException {
        assert isLocked() : "The database is locked.";
        
        final @Nullable Connection connection = Database.connection.get();
        if (connection != null) return connection;
        else {
            Database.connection.remove();
            Log.warning("Could not connect to the database.");
            throw new SQLException("Could not connect to the database.");
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Transactions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Commits all changes of the current thread since the last commit or rollback.
     * (On the {@link Server}, this method should only be called by the {@link Worker}.)
     */
    @Locked
    @Committing
    @Initialized
    public static void commit() throws SQLException {
        assert isLocked() : "The database is locked.";
        
        getConnection().commit();
    }
    
    /**
     * Rolls back all changes of the current thread since the last commit or rollback.
     * (On the {@link Server}, this method should only be called by the {@link Worker}.)
     */
    @Locked
    @Committing
    @Initialized
    public static void rollback() {
        assert isLocked() : "The database is locked.";
        
        try {
            getConnection().rollback();
        } catch (@Nonnull SQLException exception) {
            Log.error("Could not roll back.", exception);
        }
    }
    
    /**
     * Closes the connection of the current thread.
     */
    @Committing
    @Initialized
    static void close() throws SQLException {
        getConnection().close();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Savepoints –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns a savepoint for the connection of the current thread or null if not supported or required.
     * 
     * @return a savepoint for the connection of the current thread or null if not supported or required.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static @Nullable Savepoint setSavepoint() throws SQLException {
        return getConfiguration().setSavepoint(getConnection());
    }
    
    /**
     * Rolls back the connection of the current thread to the given savepoint and releases the savepoint afterwards.
     * 
     * @param savepoint the savepoint to roll the connection back to or null if not supported or required.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static void rollback(@Nullable Savepoint savepoint) throws SQLException {
        getConfiguration().rollback(getConnection(), savepoint);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Statements –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new statement on the connection of the current thread.
     * 
     * @return a new statement on the connection of the current thread.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static @Nonnull Statement createStatement() throws SQLException {
        return getConnection().createStatement();
    }
    
    /**
     * Prepares the statement on the connection of the current thread.
     * 
     * @param SQL the statement which is to be prepared for later use.
     * 
     * @return a new statement on the connection of the current thread.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static @Nonnull PreparedStatement prepareStatement(@Nonnull String SQL) throws SQLException {
        return getConnection().prepareStatement(SQL);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Conversions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the syntax for storing a boolean value.
     * 
     * @param value the value which is to be stored.
     * 
     * @return the syntax for storing a boolean value.
     */
    @Pure
    @Initialized
    public static @Nonnull String toBoolean(boolean value) {
        return getConfiguration().BOOLEAN(value);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Insertions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Executes the given insertion and returns the generated key.
     * 
     * @param statement a statement to execute the insertion.
     * @param SQL an SQL statement that inserts an entry.
     * 
     * @return the key generated for the inserted entry.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static long executeInsert(@Nonnull Statement statement, @Nonnull String SQL) throws SQLException {
        return getConfiguration().executeInsert(statement, SQL);
    }
    
    /**
     * Returns a prepared statement that can be used to insert values and retrieve their key.
     * 
     * @param SQL the insert statement which is to be prepared for returning the generated keys.
     * 
     * @return a prepared statement that can be used to insert values and retrieve their key.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static @Nonnull PreparedStatement prepareInsertStatement(@Nonnull String SQL) throws SQLException {
        return getConnection().prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
    }
    
    /**
     * Returns the key generated by the given prepared statement.
     * 
     * @param preparedStatement an executed prepared statement that has generated a key.
     * 
     * @return the key generated by the given prepared statement.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static long getGeneratedKey(@Nonnull PreparedStatement preparedStatement) throws SQLException {
        try (@Nonnull ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) return resultSet.getLong(1);
            else throw new SQLException("The given SQL statement did not generate a key.");
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Ignoring –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a rule to ignore duplicate insertions.
     * 
     * @param statement a statement to create the rule with.
     * @param table the table to which the rule is applied.
     * @param columns the columns of the primary key.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static void onInsertIgnore(@Nonnull Statement statement, @Nonnull String table, @Nonnull @NonEmpty String... columns) throws SQLException {
        getConfiguration().onInsertIgnore(statement, table, columns);
    }
    
    /**
     * Drops the rule to ignore duplicate insertions.
     * 
     * @param statement a statement to drop the rule with.
     * @param table the table from which the rule is dropped.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static void onInsertNotIgnore(@Nonnull Statement statement, @Nonnull String table) throws SQLException {
        getConfiguration().onInsertNotIgnore(statement, table);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Updating –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a rule to update duplicate insertions.
     * 
     * @param statement a statement to create the rule with.
     * @param table the table to which the rule is applied.
     * @param key the number of columns in the primary key.
     * @param columns the columns which are inserted starting with the columns of the primary key.
     * 
     * @require columns.length >= key : "At least as many columns as in the primary key are provided.";
     */
    @Locked
    @Initialized
    @NonCommitting
    public static void onInsertUpdate(@Nonnull Statement statement, @Nonnull String table, @Positive int key, @Nonnull String... columns) throws SQLException {
        getConfiguration().onInsertUpdate(statement, table, key, columns);
    }
    
    /**
     * Drops the rule to update duplicate insertions.
     * 
     * @param statement a statement to drop the rule with.
     * @param table the table from which the rule is dropped.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static void onInsertNotUpdate(@Nonnull Statement statement, @Nonnull String table) throws SQLException {
        getConfiguration().onInsertNotUpdate(statement, table);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Locking –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Locks the database if its access should be serialized.
     */
    @Initialized
    public static void lock() throws SQLException {
        getConfiguration().lock();
    }
    
    /**
     * Unlocks the database if its access has been serialized.
     */
    @Initialized
    public static void unlock() {
        if (getConfiguration() instanceof SQLiteConfiguration && ((SQLiteConfiguration) getConfiguration()).journalExists()) {
            Log.warning("A database journal exists! The connection might not have been committed properly.", new Exception());
        }
        getConfiguration().unlock();
    }
    
    /**
     * Returns whether the database is locked by the current thread.
     * 
     * @return whether the database is locked by the current thread.
     */
    @Initialized
    public static boolean isLocked() {
        return getConfiguration().isLocked();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Purging –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the timer to schedule tasks.
     */
    private static final @Nonnull Timer timer = new Timer();
    
    /**
     * Stores the tables which are to be purged regularly.
     */
    private static final @Nonnull ConcurrentMap<String, Time> tables = ConcurrentHashMap.get();
    
    /**
     * Adds the given table to the list for regular purging.
     * 
     * @param table the name of the table which is to be purged regularly.
     * @param time the time after which entries in the given table can be purged.
     */
    public static void addRegularPurging(@Nonnull String table, @Nonnull Time time) {
        tables.put(table, time);
    }
    
    /**
     * Removes the given table from the list for regular purging.
     * 
     * @param table the name of the table which is no longer to be purged.
     */
    public static void removeRegularPurging(@Nonnull String table) {
        tables.remove(table);
    }
    
    /**
     * Starts the timer for purging.
     */
    public static void startPurging() {
        // TODO: If several processes access the database, it's enough when one of them does the purging.
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Database.lock();
                    try (@Nonnull Statement statement = createStatement()) {
                        for (final @Nonnull Map.Entry<String, Time> entry : tables.entrySet()) {
                            statement.executeUpdate("DELETE FROM " + entry.getKey() + " WHERE time < " + entry.getValue().ago());
                            commit();
                        }
                    }
                } catch (@Nonnull SQLException exception) {
                    Log.warning("Could not prune a table.", exception);
                    rollback();
                } finally {
                    Database.unlock();
                }
            }
        }, Time.MINUTE.getValue(), Time.HOUR.getValue());
    }
    
    /**
     * Stops the timer for purging.
     */
    public static void stopPurging() {
        timer.cancel();
    }
    
}
