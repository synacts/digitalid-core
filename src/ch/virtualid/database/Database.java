package ch.virtualid.database;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.cache.Cache;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.io.Level;
import ch.virtualid.io.Logger;
import ch.virtualid.server.Server;
import ch.virtualid.server.Worker;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
import ch.xdf.SignatureWrapper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides connections to the database.
 * <p>
 * <em>Important:</em> The table names without the prefix may consist of at most 22 characters!
 * Moreover, if a host is run with SQLite as database, its identifier may not contain a hyphen.
 * 
 * @see Configuration
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Database implements Immutable {
    
    /**
     * Stores the logger of the database.
     */
    public static final @Nonnull Logger LOGGER = new Logger("Database.log");
    
    
    /**
     * The pattern that valid database names have to match.
     */
    private static final @Nonnull Pattern pattern = Pattern.compile("[a-z0-9_]+", Pattern.CASE_INSENSITIVE);
    
    /**
     * Returns whether the given name is valid for a database.
     * 
     * @param name the database name to check for validity.
     * 
     * @return whether the given name is valid for a database.
     */
    public static boolean isValid(@Nonnull String name) {
        return name.length() <= 40 && pattern.matcher(name).matches();
    }
    
    
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
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    @Pure
    public static @Nonnull Configuration getConfiguration() {
        assert isInitialized() : "The database is initialized.";
        
        assert configuration != null;
        return configuration;
    }
    
    
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
    
    
    /**
     * Stores the timer to schedule tasks.
     */
    private static final @Nonnull Timer timer = new Timer();
    
    /**
     * Stores the tables which are to be purged regularly.
     */
    private static final @Nonnull ConcurrentMap<String, Time> tables = new ConcurrentHashMap<String, Time>();
    
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
     * Shuts down the timer.
     */
    public static void shutDown() {
        timer.cancel();
    }
    
    
    /**
     * Loads all classes in the given directory.
     * 
     * @param directory the directory containing the classes.
     * @param prefix the path to the given directory as class prefix.
     * 
     * @require directory.isDirectory() : "The directory is indeed a directory.";
     */
    public static void loadClasses(@Nonnull File directory, @Nonnull String prefix) throws ClassNotFoundException {
        assert directory.isDirectory() : "The directory is indeed a directory.";
        
        final @Nonnull File[] files = directory.listFiles();
        for (final @Nonnull File file : files) {
            final @Nonnull String fileName = file.getName();
            if (file.isDirectory()) {
                loadClasses(file, prefix + fileName + ".");
            } else if (fileName.endsWith(".class")) {
                final @Nonnull String className = prefix + fileName.substring(0, fileName.length() - 6);
                LOGGER.log(Level.INFORMATION, "Initialize class: " + className);
                Class.forName(className);
            }
        }
    }
    
    /**
     * Loads all classes in the given jar file.
     * 
     * @param jarFile the jar file containing the classes.
     */
    public static void loadJarFile(@Nonnull JarFile jarFile) throws ClassNotFoundException {
        final @Nonnull Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final @Nonnull String entryName = entries.nextElement().getName();
            if (entryName.endsWith(".class")) {
                final @Nonnull String className = entryName.substring(0, entryName.length() - 6).replace("/", ".");
                LOGGER.log(Level.INFORMATION, "Initialize class: " + className);
                Class.forName(className);
            }
        }
    }
    
    /**
     * Loads all the classes of the current code source (either a jar or directory).
     * (All the classes need to be loaded in the main thread because otherwise their
     * type initializations might be lost by a rollback of the database transaction.)
     */
    public static void loadClasses() {
        try {
            // Ensure that the semantic type is loaded before the syntactic type.
            Class.forName(SemanticType.class.getName());
            
            // Ensure that the signature wrapper is loaded before its subclasses.
            Class.forName(SignatureWrapper.class.getName());
            
            final @Nonnull File root = new File(Database.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            LOGGER.log(Level.INFORMATION, "Root of classes: " + root);
            
            if (root.getName().endsWith(".jar")) {
                loadJarFile(new JarFile(root));
            } else {
                loadClasses(root, "");
            }
            
            Cache.initialize();
            
            commit();
        } catch (@Nonnull URISyntaxException | IOException | ClassNotFoundException | SQLException exception) {
            throw new InitializationError("Could not load all classes.", exception);
        }
        
        LOGGER.log(Level.INFORMATION, "All classes have been loaded.");
        
        // TODO: If several processes access the database, it's enough when one of them does the purging.
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try (@Nonnull Statement statement = Database.createStatement()) {
                    for (final @Nonnull Map.Entry<String, Time> entry : tables.entrySet()) {
                        statement.executeUpdate("DELETE FROM " + entry.getKey() + " WHERE time < " + entry.getValue().ago());
                        Database.commit();
                    }
                } catch (@Nonnull SQLException exception) {
                    LOGGER.log(Level.WARNING, exception);
                }
            }
        }, Time.MINUTE.getValue(), Time.HOUR.getValue());
        
        addRegularPurging("general_reply", Time.TWO_YEARS);
    }
    
    /**
     * Initializes the database with the given configuration.
     * 
     * @param configuration the configuration of the database.
     * @param singleAccess whether the database is accessed by a single process.
     * @param loading whether all the classes of the current code source are to be loaded.
     */
    public static void initialize(@Nonnull Configuration configuration, boolean singleAccess, boolean loading) {
        Database.configuration = configuration;
        Database.singleAccess = singleAccess;
        mainThread.set(true);
        connection.remove();
        
        if (loading) loadClasses();
    }
    
    /**
     * Returns whether the database has been initialized.
     * 
     * @return whether the database has been initialized.
     */
    @Pure
    public static boolean isInitialized() { return configuration != null; }
    
    
    /**
     * Stores the open connection to the database that is associated with the current thread.
     */
    private static final @Nonnull ThreadLocal<Connection> connection = new ThreadLocal<Connection>() {
        @Override protected @Nullable Connection initialValue() {
            assert configuration != null : "The database is initialized.";
            try {
                final @Nonnull Connection connection = DriverManager.getConnection(configuration.getURL(), configuration.getProperties());
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                connection.setAutoCommit(false);
                return connection;
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
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    @Pure
    private static @Nonnull Connection getConnection() throws SQLException {
        assert isInitialized() : "The database is initialized.";
        
        final @Nullable Connection connection = Database.connection.get();
        if (connection == null) {
            Database.connection.remove();
            LOGGER.log(Level.WARNING, "Could not connect to the database.");
            throw new SQLException("Could not connect to the database.");
        }
        return connection;
    }
    
    
    /**
     * Commits all changes of the current thread since the last commit or rollback.
     * (On the {@link Server}, this method should only be called by the {@link Worker}.)
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    public static void commit() throws SQLException {
        getConnection().commit();
    }
    
    /**
     * Rolls back all changes of the current thread since the last commit or rollback.
     * (On the {@link Server}, this method should only be called by the {@link Worker}.)
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    public static void rollback() throws SQLException {
        getConnection().rollback();
    }
    
    /**
     * Closes the connection of the current thread.
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    static void close() throws SQLException {
        getConnection().close();
    }
    
    
    /**
     * Returns a savepoint for the connection of the current thread or null if not supported or required.
     * 
     * @return a savepoint for the connection of the current thread or null if not supported or required.
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    public static @Nullable Savepoint setSavepoint() throws SQLException {
        return getConfiguration().setSavepoint(getConnection());
    }
    
    /**
     * Rolls back the connection of the current thread to the given savepoint and releases the savepoint afterwards.
     * 
     * @param savepoint the savepoint to roll the connection back to or null if not supported or required.
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    public static void rollback(@Nullable Savepoint savepoint) throws SQLException {
        getConfiguration().rollback(getConnection(), savepoint);
    }
    
    
    /**
     * Creates a new statement on the connection of the current thread.
     * 
     * @return a new statement on the connection of the current thread.
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    public static @Nonnull Statement createStatement() throws SQLException {
        return getConnection().createStatement();
    }
    
    /**
     * Prepares the statement on the connection of the current thread.
     * 
     * @param SQL the statement which is to be prepared for later use.
     * 
     * @return a new statement on the connection of the current thread.
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    public static @Nonnull PreparedStatement prepareStatement(@Nonnull String SQL) throws SQLException {
        return getConnection().prepareStatement(SQL);
    }
    
    
    /**
     * Executes the given insertion and returns the generated key.
     * 
     * @param statement a statement to execute the insertion.
     * @param SQL an SQL statement that inserts an entry.
     * 
     * @return the key generated for the inserted entry.
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    public static long executeInsert(@Nonnull Statement statement, @Nonnull String SQL) throws SQLException {
        return getConfiguration().executeInsert(statement, SQL);
    }
    
    /**
     * Returns a prepared statement that can be used to insert values and retrieve their key.
     * 
     * @param SQL the insert statement which is to be prepared for returning the generated keys.
     * 
     * @return a prepared statement that can be used to insert values and retrieve their key.
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    public static @Nonnull PreparedStatement prepareInsertStatement(@Nonnull String SQL) throws SQLException {
        return getConnection().prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
    }
    
    /**
     * Returns the key generated by the given prepared statement.
     * 
     * @param preparedStatement an executed prepared statement that has generated a key.
     * 
     * @return the key generated by the given prepared statement.
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    public static long getGeneratedKey(@Nonnull PreparedStatement preparedStatement) throws SQLException {
        try (@Nonnull ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) return resultSet.getLong(1);
            else throw new SQLException("The given SQL statement did not generate a key.");
        }
    }
    
    
    /**
     * Creates a rule to ignore duplicate insertions.
     * 
     * @param statement a statement to create the rule with.
     * @param table the table to which the rule is applied.
     * @param columns the columns of the primary key.
     * 
     * @require isInitialized() : "The database is initialized.";
     * @require columns.length > 0 : "At least one column is provided.";
     */
    public static void onInsertIgnore(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) throws SQLException {
        getConfiguration().onInsertIgnore(statement, table, columns);
    }
    
    /**
     * Drops the rule to ignore duplicate insertions.
     * 
     * @param statement a statement to drop the rule with.
     * @param table the table from which the rule is dropped.
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    public static void onInsertNotIgnore(@Nonnull Statement statement, @Nonnull String table) throws SQLException {
        getConfiguration().onInsertNotIgnore(statement, table);
    }
    
    
    /**
     * Creates a rule to update duplicate insertions.
     * 
     * @param statement a statement to create the rule with.
     * @param table the table to which the rule is applied.
     * @param key the number of columns in the primary key.
     * @param columns the columns which are inserted starting with the columns of the primary key.
     * 
     * @require isInitialized() : "The database is initialized.";
     * @require key > 0 : "The number of columns in the primary key is positive.";
     * @require columns.length >= key : "At least as many columns as in the primary key are provided.";
     */
    public static void onInsertUpdate(@Nonnull Statement statement, @Nonnull String table, int key, @Nonnull String... columns) throws SQLException {
        getConfiguration().onInsertUpdate(statement, table, key, columns);
    }
    
    /**
     * Drops the rule to update duplicate insertions.
     * 
     * @param statement a statement to drop the rule with.
     * @param table the table from which the rule is dropped.
     * 
     * @require isInitialized() : "The database is initialized.";
     */
    public static void onInsertNotUpdate(@Nonnull Statement statement, @Nonnull String table) throws SQLException {
        getConfiguration().onInsertNotUpdate(statement, table);
    }
    
}
