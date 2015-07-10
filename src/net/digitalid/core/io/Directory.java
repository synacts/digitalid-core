package net.digitalid.core.io;

import java.io.File;
import java.io.FilenameFilter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Initialized;
import net.digitalid.core.annotations.IsDirectory;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Stateless;
import net.digitalid.core.errors.InitializationError;

/**
 * This class provides references to all directories that are used by this implementation.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Stateless
public final class Directory {
    
    /**
     * References the default directory to store all other directories.
     */
    public static final @Nonnull @IsDirectory File DEFAULT = new File(System.getProperty("user.home") + File.separator + ".DigitalID");
    
    /**
     * Stores the directory that contains all other directories.
     */
    private static @Nullable @IsDirectory File root;
    
    /**
     * Initializes this class with the given root directory.
     * 
     * @param root the directory that contains all other directories.
     */
    public static void initialize(@Nonnull @IsDirectory File root) {
        Directory.root = root;
    }
    
    /**
     * Returns whether this class is initialized.
     * 
     * @return whether this class is initialized.
     */
    public static boolean isInitialized() {
        return root != null;
    }
    
    
    /**
     * Creates the directory with the given name in the root directory.
     * 
     * @param name the name of the directory which is to be created.
     * 
     * @return the newly created directory with the given name.
     * 
     * @ensure return.exists() : "The returned directory exists.";
     */
    @Initialized
    private static @Nonnull @IsDirectory File createDirectory(@Nonnull String name) {
        assert root != null : "This class is initialized.";
        
        final @Nonnull File directory = new File(root.getPath() + File.separator + name);
        if (!directory.exists() && !directory.mkdirs()) throw new InitializationError("Could not create the directory '" + directory.getPath() + "'.");
        return directory;
    }
    
    /**
     * Returns the directory that contains the log files.
     * 
     * @return the directory that contains the log files.
     * 
     * @ensure return.exists() : "The returned directory exists.";
     */
    @Initialized
    public static @Nonnull @IsDirectory File getLogsDirectory() {
        return createDirectory("Logs");
    }
    
    /**
     * Returns the directory that contains the configuration or the data of the database.
     * 
     * @return the directory that contains the configuration or the data of the database.
     * 
     * @ensure return.exists() : "The returned directory exists.";
     */
    @Initialized
    public static @Nonnull @IsDirectory File getDataDirectory() {
        return createDirectory("Data");
    }
    
    /**
     * Returns the directory that contains the secret key of each local client.
     * 
     * @return the directory that contains the secret key of each local client.
     * 
     * @ensure return.exists() : "The returned directory exists.";
     */
    @Initialized
    public static @Nonnull @IsDirectory File getClientsDirectory() {
        return createDirectory("Clients");
    }
    
    /**
     * Returns the directory that contains the key pairs of each local host.
     * 
     * @return the directory that contains the key pairs of each local host.
     * 
     * @ensure return.exists() : "The returned directory exists.";
     */
    @Initialized
    public static @Nonnull @IsDirectory File getHostsDirectory() {
        return createDirectory("Hosts");
    }
    
    /**
     * Returns the directory that contains the code of all installed services.
     * 
     * @return the directory that contains the code of all installed services.
     * 
     * @ensure return.exists() : "The returned directory exists.";
     */
    @Initialized
    public static @Nonnull @IsDirectory File getServicesDirectory() {
        return createDirectory("Services");
    }
    
    
    /**
     * Stores a filter to ignore hidden files.
     */
    private final static @Nonnull IgnoreHiddenFilesFilter ignoreHiddenFilesFilter = new IgnoreHiddenFilesFilter();
    
    /**
     * Returns a list of the non-hidden files in the given directory.
     * 
     * @param directory the directory whose files are to be returned.
     * 
     * @return a list of the non-hidden files in the given directory.
     */
    @Pure
    public static @Nonnull File[] listFiles(@Nonnull @IsDirectory File directory) {
        return directory.listFiles(ignoreHiddenFilesFilter);
    }
    
    
    /**
     * Deletes the given file or directory and returns whether it was successful.
     * 
     * @param file the file or directory to be deleted.
     * 
     * @return {@code true} if the given file was successfully deleted, {@code false} otherwise.
     */
    public static boolean delete(@Nonnull File file) {
        if (file.isDirectory()) {
            final @Nonnull File[] subfiles = file.listFiles();
            for (@Nonnull File subfile : subfiles) {
                if (!delete(subfile)) return false;
            }
        }
        return file.delete();
    }
    
    /**
     * Empties the given directory and returns whether it was successful.
     * 
     * @param directory the directory to be emptied.
     * 
     * @return {@code true} if the given file was successfully emptied, {@code false} otherwise.
     */
    public static boolean empty(@Nonnull @IsDirectory File directory) {
        final @Nonnull File[] subfiles = directory.listFiles();
        for (@Nonnull File subfile : subfiles) {
            if (!delete(subfile)) return false;
        }
        return true;
    }
    
}


/**
 * This class implements a filter to ignore hidden files.
 */
class IgnoreHiddenFilesFilter implements FilenameFilter {
    
    @Pure
    @Override
    public boolean accept(@Nonnull File directory, @Nonnull String name) {
        return !name.startsWith(".");
    }
    
}
