package ch.virtualid.database;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exception.InitializationError;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.io.Level;
import ch.virtualid.io.Logger;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides connections to the database.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Database implements Immutable {
    
    /**
     * Stores the logger of the database.
     */
    private static final @Nonnull Logger logger = new Logger("Database.log");
    
    
    /**
     * Stores the configuration of the database.
     */
    private static @Nullable Configuration configuration;
    
    /**
     * Returns the configuration of the database.
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
     * Initializes all classes in the given directory.
     * 
     * @param directory the directory containing the classes.
     * @param prefix the path to the given directory as class prefix.
     * 
     * @require directory.isDirectory() : "The directory is indeed a directory.";
     */
    public static void initializeClasses(@Nonnull File directory, @Nonnull String prefix) throws ClassNotFoundException {
        assert directory.isDirectory() : "The directory is indeed a directory.";
        
        final @Nonnull File[] files = directory.listFiles();
        for (final @Nonnull File file : files) {
            final @Nonnull String fileName = file.getName();
            if (file.isDirectory()) {
                initializeClasses(file, prefix + fileName + ".");
            } else if (fileName.endsWith(".class")) {
                final @Nonnull String className = prefix + fileName.substring(0, fileName.length() - 6);
                System.out.println("Initialize class: " + className);
                Class.forName(className);
            }
        }
    }
    
    /**
     * Initializes all classes in the given jar file.
     * 
     * @param jarFile the jar file containing the classes.
     */
    public static void initializeJarFile(@Nonnull JarFile jarFile) throws ClassNotFoundException {
        final @Nonnull Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final @Nonnull String entryName = entries.nextElement().getName();
            if (entryName.endsWith(".class")) {
                final @Nonnull String className = entryName.substring(0, entryName.length() - 6).replace("/", ".");
                System.out.println("Initialize class: " + className);
                Class.forName(className);
            }
        }
    }
    
    /**
     * Initializes all the modules that depend on the database.
     * Modules need to be initialized in the main thread because
     * otherwise their initialization might be lost by a rollback.
     * 
     * @param configuration the configuration of the database.
     */
    public static void initialize(@Nonnull Configuration configuration) {
        Database.configuration = configuration;
        mainThread.set(true);
        
        try {
            final @Nonnull File root = new File(Database.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            System.out.println("Root of classes: " + root);

            if (root.getName().endsWith(".jar")) {
                initializeJarFile(new JarFile(root));
            } else {
                initializeClasses(root, "");
            }
            
            getConnection().commit();
        } catch (@Nonnull URISyntaxException | IOException | ClassNotFoundException | SQLException exception) {
            throw new InitializationError("Could not load all classes.", exception);
        }
        
        logger.log(Level.INFORMATION, "All classes have been loaded.");
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
    public static @Nonnull Connection getConnection() throws SQLException {
        assert isInitialized() : "The database is initialized.";
        
        final @Nullable Connection connection = Database.connection.get();
        if (connection == null) {
            Database.connection.remove();
            logger.log(Level.WARNING, "Could not connect to the database.");
            throw new SQLException("Could not connect to the database.");
        }
        return connection;
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
    
}
