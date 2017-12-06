/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// TODO: This class is no longer necessary but might still be a useful reference for a plugin mechanism.

package net.digitalid.core.server;

//import java.io.File;
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.util.Enumeration;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.jar.JarEntry;
//import java.util.jar.JarFile;
//
//import javax.annotation.Nonnull;
//
//import net.digitalid.utility.logging.Log;
//import net.digitalid.utility.validation.annotations.type.Stateless;
//
//import net.digitalid.database.annotations.transaction.Committing;
//import net.digitalid.database.exceptions.operation.FailedCommitException;
//import net.digitalid.database.interfaces.Database;
//
///**
// * This class loads other classes in other domains.
// */
//@Stateless
public abstract class Loader {
//    
//    /**
//     * Loads all classes in the given directory.
//     * 
//     * @param directory the directory containing the classes.
//     * @param prefix the path to the given directory as class prefix.
//     */
//    @Committing
//    private static void loadClasses(@Nonnull @IsDirectory File directory, @Nonnull String prefix) throws ClassNotFoundException, FailedCommitException {
//        final @Nonnull File[] files = directory.listFiles();
//        for (final @Nonnull File file : files) {
//            final @Nonnull String fileName = file.getName();
//            if (file.isDirectory()) {
//                loadClasses(file, prefix + fileName + ".");
//            } else if (fileName.endsWith(".class")) {
//                final @Nonnull String className = prefix + fileName.substring(0, fileName.length() - 6);
//                Log.debugging("Initialize the class '" + className + "'.");
//                Class.forName(className);
//            }
//        }
//        Database.commit();
//    }
//    
//    /**
//     * Loads all classes in the given jar file.
//     * 
//     * @param jarFile the jar file containing the classes.
//     */
//    @Committing
//    public static void loadJarFile(@Nonnull JarFile jarFile) throws ClassNotFoundException, FailedCommitException, MalformedURLException {
//        final @Nonnull URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{ new File(jarFile.getName()).toURI().toURL() }, Loader.class.getClassLoader());
//        final @Nonnull Enumeration<JarEntry> entries = jarFile.entries();
//        while (entries.hasMoreElements()) {
//            final @Nonnull String entryName = entries.nextElement().getName();
//            Log.verbose("Found the entry '" + entryName + "'.");
//            if (entryName.startsWith("net/digitalid/") && entryName.endsWith(".class")) {
//                final @Nonnull String className = entryName.substring(0, entryName.length() - 6).replace("/", ".");
//                Log.debugging("Initialize the class '" + className + "'.");
//                Class.forName(className, true, urlClassLoader);
//            }
//        }
//        Database.commit();
//    }
//    
//    /**
//     * Stores the roots of classes which have already been loaded.
//     */
//    private static final @Nonnull Set<File> roots = new HashSet<>();
//    
//    /**
//     * Loads all the classes of the given code source (either a jar or directory).
//     * (All the classes need to be loaded in the main thread because otherwise their
//     * type initializations might be lost by a rollback of the database transaction.)
//     * 
//     * @param mainClass the main class which is used to determine the code source.
//     * @param preponedClasses the classes that are to be loaded before the others.
//     */
//    @Committing
//    private static void loadClasses(@Nonnull Class<?> mainClass, @Nonnull Class<?>... preponedClasses) {
//        try {
//            for (final @Nonnull Class<?> preponedClass : preponedClasses) {
//                Log.debugging("Initialize the preponed class '" + preponedClass.getName() + "'.");
//                Class.forName(preponedClass.getName());
//            }
//            
//            final @Nonnull File root = new File(mainClass.getProtectionDomain().getCodeSource().getLocation().toURI());
//            Log.debugging("The root of classes for " + mainClass.getSimpleName() + " is '" + root + "'.");
//            
//            if (roots.contains(root)) {
//                Log.debugging("The classes in '" + root + "' have already been loaded.");
//            } else {
//                if (root.getName().endsWith(".jar")) {
//                    loadJarFile(new JarFile(root));
//                } else {
//                    loadClasses(root, "");
//                }
//                roots.add(root);
//                Log.debugging("All classes in '" + root + "' have been loaded.");
//            }
//        } catch (@Nonnull URISyntaxException | IOException | ClassNotFoundException | FailedOperationException exception) {
//            throw InitializationError.get("Could not load all classes.", exception);
//        }
//    }
//    
//    /**
//     * Initializes all the classes of Digital ID and the given libraries.
//     * 
//     * @param mainClasses the main classes of the libraries to be loaded.
//     */
//    @Committing
//    public static void initialize(@Nonnull Class<?>... mainClasses) {
//        Loader.loadClasses(Server.class, SemanticType.class, SignatureWrapper.class);
//        Database.addRegularPurging("general_reply", Time.TWO_YEARS.getValue());
//        Database.startPurging();
//        Cache.initialize();
//        
//        for (final @Nonnull Class<?> mainClass : mainClasses) {
//            Loader.loadClasses(mainClass);
//        }
//    }
//    
}
