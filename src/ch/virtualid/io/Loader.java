package ch.virtualid.io;

import ch.virtualid.annotations.Committing;
import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.database.Database;
import ch.virtualid.errors.InitializationError;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.annotation.Nonnull;

/**
 * This class loads other classes in other domains.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Loader {
    
    /**
     * Stores the logger of the loader.
     */
    private static final @Nonnull Logger LOGGER = new Logger("Loader.log");
    
    /**
     * Loads all classes in the given directory.
     * 
     * @param directory the directory containing the classes.
     * @param prefix the path to the given directory as class prefix.
     * 
     * @require directory.isDirectory() : "The directory is indeed a directory.";
     */
    @NonCommitting
    private static void loadClasses(@Nonnull File directory, @Nonnull String prefix) throws ClassNotFoundException, SQLException {
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
        Database.commit();
    }
    
    /**
     * Loads all classes in the given jar file.
     * 
     * @param jarFile the jar file containing the classes.
     */
    @NonCommitting
    public static void loadJarFile(@Nonnull JarFile jarFile) throws ClassNotFoundException, SQLException, MalformedURLException {
        final @Nonnull URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{ new File(jarFile.getName()).toURI().toURL() }, Loader.class.getClassLoader());
        final @Nonnull Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final @Nonnull String entryName = entries.nextElement().getName();
            if (entryName.endsWith(".class")) {
                final @Nonnull String className = entryName.substring(0, entryName.length() - 6).replace("/", ".");
                LOGGER.log(Level.INFORMATION, "Initialize class: " + className);
                Class.forName(className, true, urlClassLoader);
            }
        }
        Database.commit();
    }
    
    /**
     * Loads all the classes of the given code source (either a jar or directory).
     * (All the classes need to be loaded in the main thread because otherwise their
     * type initializations might be lost by a rollback of the database transaction.)
     * 
     * @param mainClass the main class which is used to determine the code source.
     * @param preponedClasses the classes that are to be loaded before the others.
     */
    @Committing
    public static void loadClasses(@Nonnull Class<?> mainClass, @Nonnull Class<?>... preponedClasses) {
        try {
            for (final @Nonnull Class<?> preponedClass : preponedClasses) {
                Class.forName(preponedClass.getName());
            }
            
            final @Nonnull File root = new File(mainClass.getProtectionDomain().getCodeSource().getLocation().toURI());
            LOGGER.log(Level.INFORMATION, "Root of classes: " + root);
            
            if (root.getName().endsWith(".jar")) {
                loadJarFile(new JarFile(root));
            } else {
                loadClasses(root, "");
            }
            
            LOGGER.log(Level.INFORMATION, "All classes have been loaded.");
        } catch (@Nonnull URISyntaxException | IOException | ClassNotFoundException | SQLException exception) {
            throw new InitializationError("Could not load all classes.", exception);
        }
    }
    
}
