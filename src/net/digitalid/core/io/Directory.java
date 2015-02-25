package net.digitalid.core.io;

import java.io.File;
import java.io.FilenameFilter;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.IsDirectory;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.errors.InitializationError;

/**
 * This class provides references to all directories that are used by this implementation.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Directory {
    
    /**
     * Stores the file separator of the current operating system.
     */
    public static final @Nonnull String SEPARATOR = System.getProperty("file.separator");
    
    /**
     * Reference to the root directory of all files that are used by this implementation.
     */
    public static final @Nonnull @IsDirectory File ROOT = new File(System.getProperty("user.home") + SEPARATOR + ".DigitalID");
    
    /**
     * Reference to the directory that contains the configuration of the database and its data.
     */
    public static final @Nonnull @IsDirectory File DATA = new File(ROOT.getPath() +  SEPARATOR + "Data");
    
    /**
     * Reference to the clients directory that contains the configuration of each local client.
     */
    public static final @Nonnull @IsDirectory File CLIENTS = new File(ROOT.getPath() +  SEPARATOR + "Clients");
    
    /**
     * Reference to the hosts directory that contains the configuration of each local host.
     */
    public static final @Nonnull @IsDirectory File HOSTS = new File(ROOT.getPath() +  SEPARATOR + "Hosts");
    
    /**
     * Reference to the log directory that contains the various log files.
     */
    public static final @Nonnull @IsDirectory File LOGS = new File(ROOT.getPath() +  SEPARATOR + "Logs");
    
    /**
     * Reference to the services directory that contains the code of all installed services.
     */
    public static final @Nonnull @IsDirectory File SERVICES = new File(ROOT.getPath() +  SEPARATOR + "Services");
    
    /**
     * Ensures that all referenced directories do exist.
     */
    static {
        for (@Nonnull @IsDirectory File directory : new File[] {ROOT, DATA, CLIENTS, HOSTS, LOGS, SERVICES}) {
            if (!directory.exists() && !directory.mkdirs()) throw new InitializationError("Could not make the directory '" + directory.getPath() + "'.");
        }
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
