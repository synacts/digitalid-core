package ch.virtualid.io;

import ch.virtualid.annotations.Pure;
import ch.virtualid.errors.InitializationError;
import java.io.File;
import java.io.FilenameFilter;
import javax.annotation.Nonnull;

/**
 * This class provides references to all directories that are used by this implementation.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
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
    public static final @Nonnull File ROOT = new File(System.getProperty("user.home") + SEPARATOR + ".VirtualID");
    
    /**
     * Reference to the directory that contains the configuration of the database and its data.
     */
    public static final @Nonnull File DATA = new File(ROOT.getPath() +  SEPARATOR + "Data");
    
    /**
     * Reference to the clients directory that contains the configuration of each local client.
     */
    public static final @Nonnull File CLIENTS = new File(ROOT.getPath() +  SEPARATOR + "Clients");
    
    /**
     * Reference to the hosts directory that contains the configuration of each local host.
     */
    public static final @Nonnull File HOSTS = new File(ROOT.getPath() +  SEPARATOR + "Hosts");
    
    /**
     * Reference to the log directory that contains the various log files.
     */
    public static final @Nonnull File LOGS = new File(ROOT.getPath() +  SEPARATOR + "Logs");
    
    /**
     * Reference to the services directory that contains the code of all installed services.
     */
    public static final @Nonnull File SERVICES = new File(ROOT.getPath() +  SEPARATOR + "Services");
    
    /**
     * Ensures that all referenced directories do exist.
     */
    static {
        final @Nonnull File[] directories = {ROOT, DATA, CLIENTS, HOSTS, LOGS, SERVICES};
        
        for (@Nonnull File directory : directories) {
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
     * 
     * @require directory.isDirectory() : "The given file is a directory.";
     */
    @Pure
    public static @Nonnull File[] listFiles(@Nonnull File directory) {
        assert directory.isDirectory() : "The given file is a directory.";
        
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
     * 
     * @require directory.isDirectory() : "The given file is a directory.";
     */
    public static boolean empty(@Nonnull File directory) {
        assert directory.isDirectory() : "The given file is a directory.";
        
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
