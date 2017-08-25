package net.digitalid.core.server;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.console.Console;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.logging.Level;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.Committing;

import net.digitalid.core.host.HostBuilder;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.packet.Request;

/**
 * The server runs the configured hosts.
 */
@Utility
public abstract class Server {
    
    /* -------------------------------------------------- Services -------------------------------------------------- */
    
    /**
     * Loads all services with their code in the services directory.
     */
    @Impure
    @Committing
    public static void loadServices() {
        // TODO:
        
//        final @Nonnull File[] files = Directory.getServicesDirectory().listFiles();
//        for (final @Nonnull File file : files) {
//            if (file.isFile() && file.getName().endsWith(".jar")) {
//                try {
//                    Loader.loadJarFile(new JarFile(file));
//                } catch (@Nonnull IOException | ClassNotFoundException | SQLException exception) {
//                    throw InitializationError.get("Could not load the service in the file '" + file.getName() + "'.", exception);
//                }
//            }
//        }
    }
    
    /* -------------------------------------------------- Listener -------------------------------------------------- */
    
    /**
     * References the thread that listens on the socket.
     */
    private static @Nullable Listener listener;
    
    /**
     * Starts the server with the configured hosts.
     */
    @Impure
    @Committing
    public static void start() throws IOException {
        loadServices();
        
        listener = ListenerBuilder.build();
        listener.start();
        
//        try {
//            Cache.getPublicKeyChain(HostIdentity.DIGITALID);
//            Database.commit();
//        } catch (@Nonnull DatabaseException exception) {
//            throw InitializationError.get("Could not retrieve the public key chain of 'digitalid.net'.", exception);
//        }
    }
    
    /**
     * Stops the background threads of the server without shutting down (which is important for testing purposes).
     */
    @Impure
    public static void stop() {
        if (listener != null) {
            listener.shutDown();
        }
//        Client.stop();
    }
    
    /**
     * Shuts down the server after having handled all pending requests.
     */
    @Impure
    public static void shutDown() {
        Server.stop();
        System.exit(0);
    }
    
    /* -------------------------------------------------- Main Method -------------------------------------------------- */
    
    /**
     * The main method starts the server with the configured hosts and shows the console.
     * 
     * @param arguments the command line arguments indicating the hosts to be created when starting up.
     */
    @Impure
    @Committing
    public static void main(@Nonnull String[] arguments) {
        try {
            Configuration.initializeAllConfigurations();
            
            Console.writeLine();
            Console.writeLine("The library has been initialized successfully.");
            
//        try {
//            if (MySQLConfiguration.exists()) { configuration = new MySQLConfiguration(false); }
//            else if (PostgreSQLConfiguration.exists()) { configuration = new PostgreSQLConfiguration(false); }
//            else if (SQLiteConfiguration.exists()) { configuration = new SQLiteConfiguration(false); }
//            else {
//                Console.writeLine();
//                Console.writeLine("Please select one of the following databases:");
//                Console.writeLine("- 1: MySQL (default)");
//                Console.writeLine("- 2: PostgreSQL");
//                Console.writeLine("- 3: SQLite");
//                Console.writeLine();
//                final int input = Console.readNumber("Choice: ", 1);
//                if (input == 1) { configuration = new MySQLConfiguration(false); }
//                else if (input == 2) { configuration = new PostgreSQLConfiguration(false); }
//                else if (input == 3) { configuration = new SQLiteConfiguration(false); }
//                else {
//                    Console.writeLine(Integer.toString(input) + " was not a valid option.");
//                    Console.writeLine();
//                    return;
//                }
//            }
//        } catch (@Nonnull Exception exception) {
//            throw InitializationError.get("Could not load the database configuration.", exception);
//        }
            
            Server.start();
            Console.writeLine("The server has been started and is now listening on port $.", Request.PORT.get());
            
            for (final @Nonnull String argument : arguments) {
                Console.writeLine();
                if (HostIdentifier.isValid(argument)) {
                    Console.writeLine("Creating a host with the identifier $, which can take several minutes.", argument);
                    try {
                        HostBuilder.withIdentifier(HostIdentifier.with(argument)).build();
                    } catch (@Nonnull ExternalException exception) {
                        Console.log(Level.FATAL, "Failed to create a new host with the identifier $.", exception, argument);
                        shutDown();
                    }
                } else {
                    Console.log(Level.FATAL, "$ is not a valid host identifier!", argument);
                    shutDown();
                }
            }
            
            Options.start();
        } catch (@Nonnull Throwable throwable) {
            Console.log(Level.FATAL, "The server crashed due to the following problem.", throwable);
            shutDown();
        }
    }
    
}
