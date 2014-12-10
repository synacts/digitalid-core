package ch.virtualid.server;

import ch.virtualid.database.Configuration;
import ch.virtualid.database.Database;
import ch.virtualid.database.MySQLConfiguration;
import ch.virtualid.database.PostgreSQLConfiguration;
import ch.virtualid.database.SQLiteConfiguration;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.io.Console;
import ch.virtualid.io.Directory;
import ch.virtualid.util.FreezableLinkedHashMap;
import ch.virtualid.util.FreezableMap;
import ch.virtualid.util.ReadonlyCollection;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.jar.JarFile;
import javax.annotation.Nonnull;

/**
 * The server runs the configured hosts.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Server {
    
    /**
     * The version of the Virtual ID server implementation.
     */
    public static final @Nonnull String VERSION = "0.8 (18 October 2014)";
    
    /**
     * The authors of the Virtual ID server implementation.
     */
    public static final @Nonnull String AUTHORS = "Kaspar Etter (kaspar.etter@virtualid.ch)";
    
    /**
     * The server listens on the given port number.
     */
    public static final int PORT = 1988;
    
    
    /**
     * References the thread that listens on the socket.
     */
    private static final @Nonnull Listener listener = new Listener();
    
    /**
     * Maps the identifiers of the hosts that are running on this server to their instances.
     */
    private static final @Nonnull FreezableMap<HostIdentifier, Host> hosts = new FreezableLinkedHashMap<HostIdentifier, Host>();
    
    /**
     * Returns whether the host with the given identifier is running on this server.
     * 
     * @param hostIdentifier the identifier of the host which is to be checked.
     * 
     * @return whether the host with the given identifier is running on this server.
     */
    public static boolean hasHost(@Nonnull HostIdentifier hostIdentifier) {
        return hosts.containsKey(hostIdentifier);
    }
    
    /**
     * Returns the host with the given identifier that is running on this server.
     * 
     * @param hostIdentifier the identifier of the host which is to be returned.
     * 
     * @return the host with the given identifier that is running on this server.
     * 
     * @require hasHost(identifier) : "The host is running on this server.";
     */
    public static @Nonnull Host getHost(@Nonnull HostIdentifier hostIdentifier) {
        assert hasHost(hostIdentifier) : "The host is running on this server.";
        
        return hosts.get(hostIdentifier);
    }
    
    /**
     * Returns the hosts that are running on this server.
     * 
     * @return the hosts that are running on this server.
     * 
     * @ensure return.doesNotContainNull() : "The returned collection does not contain null.";
     */
    public static @Nonnull ReadonlyCollection<Host> getHosts() {
        return hosts.values();
    }
    
    /**
     * Adds the given host to the list of running hosts.
     * 
     * @param host the host to add.
     */
    public static void addHost(@Nonnull Host host) {
        hosts.put(host.getIdentifier(), host);
    }
    
    /**
     * Loads all hosts with a configuration but without a tables file in the hosts directory.
     */
    private static void loadHosts() {
        final @Nonnull File[] files = Directory.HOSTS.listFiles();
        for (final @Nonnull File file : files) {
            if (!file.isDirectory() && file.getName().endsWith(".private.xdf")) {
                try {
                    final @Nonnull HostIdentifier identifier = new HostIdentifier(file.getName().substring(0, file.getName().length() - 12));
                    if (!new File(Directory.HOSTS.getPath() + Directory.SEPARATOR + identifier.getString() + ".tables.xdf").exists()) new Host(identifier);
                } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
                    throw new InitializationError("Could not load the host configured in the file '" + file.getName() + "'.", exception);
                }
            }
        }
    }
    
    
    /**
     * Loads all services with their code in the services directory.
     */
    public static void loadServices() {
        final @Nonnull File[] files = Directory.SERVICES.listFiles();
        for (final @Nonnull File file : files) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try {
                    Database.loadJarFile(new JarFile(file));
                } catch (@Nonnull IOException | ClassNotFoundException exception) {
                    throw new InitializationError("Could not load the service in the file '" + file.getName() + "'.", exception);
                }
            }
        }
    }
    
    
    /**
     * Starts the server with the configured and given hosts.
     * 
     * @param arguments the identifiers of hosts to be created when starting up.
     */
    public static void start(@Nonnull String... arguments) {
        loadServices();
        loadHosts();
        
        for (final @Nonnull String argument : arguments) {
            try {
                new Host(new HostIdentifier(argument));
            } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
                throw new InitializationError("Could not create the host '" + argument + "'.", exception);
            }
        }
        
        listener.start();
        
        // TODO: Uncomment the following lines:
//        try {
//            Cache.getPublicKeyChain(HostIdentity.VIRTUALID);
//        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
//            throw new InitializationError("Could not retrieve the public key chain of 'virtualid.ch'.", exception);
//        }
    }
    
    /**
     * Stops the server without shutting down (which is important for testing purposes).
     */
    public static void stop() {
        listener.shutDown();
        hosts.clear();
    }
    
    /**
     * Shuts down the server after having handled all pending requests.
     */
    public static void shutDown() {
        listener.shutDown();
        System.exit(0);
    }
    
    /**
     * The main method starts the server with the configured hosts and shows the console.
     * 
     * @param arguments the command line arguments indicating the hosts to be created when starting up.
     */
    public static void main(@Nonnull String[] arguments) {
        final @Nonnull Configuration configuration;
        try {
            if (MySQLConfiguration.exists()) configuration = new MySQLConfiguration();
            else if (PostgreSQLConfiguration.exists()) configuration = new PostgreSQLConfiguration();
            else if (SQLiteConfiguration.exists()) configuration = new SQLiteConfiguration();
            else {
                Console.write();
                Console.write("Please select one of the following databases:");
                Console.write("- 1: MySQL");
                Console.write("- 2: PostgreSQL");
                Console.write("- 3: SQLite");
                Console.write();
                final int input = Console.readInt("Choice: ");
                if (input == 1) configuration = new MySQLConfiguration();
                else if (input == 2) configuration = new PostgreSQLConfiguration();
                else if (input == 3) configuration = new SQLiteConfiguration();
                else {
                    Console.write(Integer.toString(input) + " was not a valid option.");
                    Console.write();
                    return;
                }
            }
        } catch (@Nonnull SQLException | IOException exception) {
            throw new InitializationError("Could not load the database configuration.", exception);
        }
        Database.initialize(configuration, false, true);
        start(arguments);
        Options.start();
    }
    
}
