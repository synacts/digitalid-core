package net.digitalid.core.io;

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
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.IsDirectory;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.database.Database;
import net.digitalid.core.errors.InitializationError;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.server.Server;
import net.digitalid.core.wrappers.SignatureWrapper;

/**
 * This class loads other classes in other domains.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Loader {
    
    /**
     * Loads all classes in the given directory.
     * 
     * @param directory the directory containing the classes.
     * @param prefix the path to the given directory as class prefix.
     */
    @Locked
    @Committing
    private static void loadClasses(@Nonnull @IsDirectory File directory, @Nonnull String prefix) throws ClassNotFoundException, SQLException {
        final @Nonnull File[] files = directory.listFiles();
        for (final @Nonnull File file : files) {
            final @Nonnull String fileName = file.getName();
            if (file.isDirectory()) {
                loadClasses(file, prefix + fileName + ".");
            } else if (fileName.endsWith(".class")) {
                final @Nonnull String className = prefix + fileName.substring(0, fileName.length() - 6);
                Logger.log(Level.VERBOSE, "Loader", "Initialize class: " + className);
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
    @Locked
    @Committing
    public static void loadJarFile(@Nonnull JarFile jarFile) throws ClassNotFoundException, SQLException, MalformedURLException {
        final @Nonnull URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{ new File(jarFile.getName()).toURI().toURL() }, Loader.class.getClassLoader());
        final @Nonnull Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final @Nonnull String entryName = entries.nextElement().getName();
            Logger.log(Level.VERBOSE, "Loader", "Entry found: " + entryName);
            if (entryName.startsWith("net/digitalid/") && entryName.endsWith(".class")) {
                final @Nonnull String className = entryName.substring(0, entryName.length() - 6).replace("/", ".");
                Logger.log(Level.VERBOSE, "Loader", "Initialize class: " + className);
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
    private static void loadClasses(@Nonnull Class<?> mainClass, @Nonnull Class<?>... preponedClasses) {
        try {
            Database.lock();
            for (final @Nonnull Class<?> preponedClass : preponedClasses) {
                Class.forName(preponedClass.getName());
            }
            
            final @Nonnull File root = new File(mainClass.getProtectionDomain().getCodeSource().getLocation().toURI());
            Logger.log(Level.DEBUGGING, "Loader", "Root of classes: " + root);
            
            if (root.getName().endsWith(".jar")) {
                loadJarFile(new JarFile(root));
            } else {
                loadClasses(root, "");
            }
            
            Logger.log(Level.DEBUGGING, "Loader", "All classes have been loaded.");
        } catch (@Nonnull URISyntaxException | IOException | ClassNotFoundException | SQLException exception) {
            throw new InitializationError("Could not load all classes.", exception);
        } finally {
            Database.unlock();
        }
    }
    
    /**
     * Initializes all the classes of Digital ID and the given libraries.
     * 
     * @param mainClasses the main classes of the libraries to be loaded.
     */
    @Committing
    public static void initialize(@Nonnull Class<?>... mainClasses) {
        Loader.loadClasses(Server.class, SemanticType.class, SignatureWrapper.class);
        Database.addRegularPurging("general_reply", Time.TWO_YEARS);
        Database.startPurging();
        Cache.initialize();
        
        for (final @Nonnull Class<?> mainClass : mainClasses) {
            Loader.loadClasses(mainClass);
        }
    }
    
}
