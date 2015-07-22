package net.digitalid.core.server;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.jar.JarFile;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonLocked;
import net.digitalid.core.annotations.Stateless;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.client.Client;
import net.digitalid.core.collections.FreezableLinkedHashMap;
import net.digitalid.core.collections.FreezableMap;
import net.digitalid.core.collections.ReadOnlyCollection;
import net.digitalid.core.database.Configuration;
import net.digitalid.core.database.Database;
import net.digitalid.core.database.MySQLConfiguration;
import net.digitalid.core.database.PostgreSQLConfiguration;
import net.digitalid.core.database.SQLiteConfiguration;
import net.digitalid.core.errors.InitializationError;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.io.Console;
import net.digitalid.core.io.DefaultLogger;
import net.digitalid.core.io.Directory;
import net.digitalid.core.io.Level;
import net.digitalid.core.io.Loader;
import net.digitalid.core.io.Logger;

/**
 * The server runs the configured hosts.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Stateless
public final class Server {
    
    /**
     * Stores the version of the Digital ID implementation.
     */
    public static final @Nonnull String VERSION = "0.6.2";
    
    /**
     * Stores the date of the Digital ID implementation.
     */
    public static final @Nonnull String DATE = "22 July 2015";
    
    /**
     * Stores the authors of the Digital ID implementation.
     */
    public static final @Nonnull String AUTHORS = "Kaspar Etter (kaspar.etter@digitalid.net)";
    
    /**
     * Stores the server listens on the given port number.
     */
    public static final int PORT = 1988;
    
    
    /**
     * References the thread that listens on the socket.
     */
    private static @Nonnull Listener listener;
    
    /**
     * Maps the identifiers of the hosts that are running on this server to their instances.
     */
    private static final @Nonnull FreezableMap<HostIdentifier, Host> hosts = new FreezableLinkedHashMap<>();
    
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
    public static @Nonnull ReadOnlyCollection<Host> getHosts() {
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
     * Loads all hosts with cryptographic keys but without a tables file in the hosts directory.
     */
    @Locked
    @Committing
    private static void loadHosts() {
        // TODO: Remove this special case when the certification mechanism is implemented.
        final @Nonnull File digitalid = new File(Directory.getHostsDirectory().getPath() + File.separator + HostIdentifier.DIGITALID.getString() + ".private.xdf");
        if (digitalid.exists() && digitalid.isFile()) {
            try {
                if (!new File(Directory.getHostsDirectory().getPath() + File.separator + HostIdentifier.DIGITALID.getString() + ".tables.xdf").exists()) new Host(HostIdentifier.DIGITALID);
            } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
                throw new InitializationError("Could not load the host configured in the file '" + digitalid.getName() + "'.", exception);
            }
        }
        
        final @Nonnull File[] files = Directory.getHostsDirectory().listFiles();
        for (final @Nonnull File file : files) {
            final @Nonnull String name = file.getName();
            if (file.isFile() && name.endsWith(".private.xdf") && !name.equals(HostIdentifier.DIGITALID.getString() + ".private.xdf")) { // TODO: Remove the special case eventually.
                try {
                    final @Nonnull HostIdentifier identifier = new HostIdentifier(name.substring(0, name.length() - 12));
                    if (!new File(Directory.getHostsDirectory().getPath() + File.separator + identifier.getString() + ".tables.xdf").exists()) new Host(identifier);
                } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
                    throw new InitializationError("Could not load the host configured in the file '" + name + "'.", exception);
                }
            }
        }
    }
    
    
    /**
     * Loads all services with their code in the services directory.
     */
    @Locked
    @Committing
    public static void loadServices() {
        final @Nonnull File[] files = Directory.getServicesDirectory().listFiles();
        for (final @Nonnull File file : files) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try {
                    Loader.loadJarFile(new JarFile(file));
                } catch (@Nonnull IOException | ClassNotFoundException | SQLException exception) {
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
    @Committing
    public static void start(@Nonnull String... arguments) {
        try {
            Database.lock();
            loadServices();
            loadHosts();
            
            for (final @Nonnull String argument : arguments) {
                try {
                    new Host(new HostIdentifier(argument));
                } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
                    throw new InitializationError("Could not create the host '" + argument + "'.", exception);
                }
            }
            
            listener = new Listener(PORT);
            listener.start();
            
            Cache.getPublicKeyChain(HostIdentity.DIGITALID);
            Database.commit();
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
            throw new InitializationError("Could not retrieve the public key chain of 'digitalid.net'.", exception);
        } finally {
            Database.unlock();
        }
    }
    
    /**
     * Stops the background threads of the server without shutting down (which is important for testing purposes).
     */
    public static void stop() {
        listener.shutDown();
        Client.stop();
        hosts.clear();
    }
    
    /**
     * Shuts down the server after having handled all pending requests.
     */
    public static void shutDown() {
        Server.stop();
        System.exit(0);
    }
    
    /**
     * The main method starts the server with the configured hosts and shows the console.
     * 
     * @param arguments the command line arguments indicating the hosts to be created when starting up.
     */
    @NonLocked
    @Committing
    public static void main(@Nonnull String[] arguments) {
        Thread.currentThread().setName("Main");
        
        Directory.initialize(Directory.DEFAULT);
        Logger.initialize(new DefaultLogger(Level.INFORMATION, "Server"));
        
        final @Nonnull Configuration configuration;
        try {
            if (MySQLConfiguration.exists()) configuration = new MySQLConfiguration(false);
            else if (PostgreSQLConfiguration.exists()) configuration = new PostgreSQLConfiguration(false);
            else if (SQLiteConfiguration.exists()) configuration = new SQLiteConfiguration(false);
            else {
                Console.write();
                Console.write("Please select one of the following databases:");
                Console.write("- 1: MySQL (default)");
                Console.write("- 2: PostgreSQL");
                Console.write("- 3: SQLite");
                Console.write();
                final int input = Console.readNumber("Choice: ", 1);
                if (input == 1) configuration = new MySQLConfiguration(false);
                else if (input == 2) configuration = new PostgreSQLConfiguration(false);
                else if (input == 3) configuration = new SQLiteConfiguration(false);
                else {
                    Console.write(Integer.toString(input) + " was not a valid option.");
                    Console.write();
                    return;
                }
            }
        } catch (@Nonnull SQLException | IOException exception) {
            throw new InitializationError("Could not load the database configuration.", exception);
        }
        
        Database.initialize(configuration, false);
        Loader.initialize();
        
        Server.start(arguments);
        Options.start();
    }
    
}
