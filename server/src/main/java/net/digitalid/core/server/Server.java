package net.digitalid.core.server;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.circumfixes.Quotes;
import net.digitalid.utility.collections.collection.ReadOnlyCollection;
import net.digitalid.utility.collections.map.FreezableLinkedHashMapBuilder;
import net.digitalid.utility.collections.map.FreezableMap;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.console.Console;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.Committing;

import net.digitalid.core.host.Host;
import net.digitalid.core.host.HostBuilder;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.packet.Request;

/**
 * The server runs the configured hosts.
 */
@Utility
public abstract class Server {
    
    /* -------------------------------------------------- Hosts -------------------------------------------------- */
    
    /**
     * Maps the identifiers of the hosts that are running on this server to their instances.
     */
    private static final @Nonnull FreezableMap<HostIdentifier, Host> hosts = FreezableLinkedHashMapBuilder.build();
    
    /**
     * Returns whether the host with the given identifier is running on this server.
     */
    @Pure
    public static boolean hasHost(@Nonnull HostIdentifier hostIdentifier) {
        return hosts.containsKey(hostIdentifier);
    }
    
    /**
     * Returns the host with the given identifier that is running on this server.
     * 
     * @require hasHost(identifier) : "The host is running on this server.";
     */
    @Pure
    public static @Nonnull Host getHost(@Nonnull HostIdentifier hostIdentifier) {
        Require.that(hasHost(hostIdentifier)).orThrow("The host is running on this server.");
        
        return hosts.get(hostIdentifier);
    }
    
    /**
     * Returns the hosts that are running on this server.
     */
    @Pure
    public static @Nonnull ReadOnlyCollection<@Nonnull Host> getHosts() {
        return hosts.values();
    }
    
    /**
     * Adds the given host to the list of running hosts.
     */
    @Impure
    public static void addHost(@Nonnull Host host) {
        hosts.put(host.getIdentifier(), host);
    }
    
    /**
     * Loads all hosts with cryptographic keys but without a tables file in the hosts directory.
     */
    @Impure
    @Committing
    private static void loadHosts() {
        // TODO:
        
//        // TODO: Remove this special case when the certification mechanism is implemented.
//        final @Nonnull File digitalid = new File(Directory.getHostsDirectory().getPath() + File.separator + HostIdentifier.DIGITALID.getString() + ".private.xdf");
//        if (digitalid.exists() && digitalid.isFile()) {
//            try {
//                if (!new File(Directory.getHostsDirectory().getPath() + File.separator + HostIdentifier.DIGITALID.getString() + ".tables.xdf").exists()) { new Host(HostIdentifier.DIGITALID); }
//            } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//                throw InitializationError.get("Could not load the host configured in the file '" + digitalid.getName() + "'.", exception);
//            }
//        }
//        
//        final @Nonnull File[] files = Directory.getHostsDirectory().listFiles();
//        for (final @Nonnull File file : files) {
//            final @Nonnull String name = file.getName();
//            if (file.isFile() && name.endsWith(".private.xdf") && !name.equals(HostIdentifier.DIGITALID.getString() + ".private.xdf")) { // TODO: Remove the special case eventually.
//                try {
//                    final @Nonnull HostIdentifier identifier = new HostIdentifier(name.substring(0, name.length() - 12));
//                    if (!new File(Directory.getHostsDirectory().getPath() + File.separator + identifier.getString() + ".tables.xdf").exists()) { new Host(identifier); }
//                } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//                    throw InitializationError.get("Could not load the host configured in the file '" + name + "'.", exception);
//                }
//            }
//        }
    }
    
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
    private static @Nonnull Listener listener;
    
    /**
     * Starts the server with the configured hosts.
     */
    @Impure
    @Committing
    public static void start() throws IOException {
        loadServices();
        loadHosts();
        
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
        listener.shutDown();
//        Client.stop();
        hosts.clear();
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
    public static void main(@Nonnull String[] arguments) throws IOException {
        Console.writeLine();
        Configuration.initializeAllConfigurations();
        Log.information("The library has been initialized.");
        Console.writeLine("The library has been initialized.");
        
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
        Log.information("The server has been started.");
        Console.writeLine("The server has been started and is now listening on port " + Request.PORT.get() + ".");
        
        for (final @Nonnull String argument : arguments) {
            Console.writeLine();
            if (HostIdentifier.isValid(argument)) {
                Console.writeLine("Creating a host with the identifier " + Quotes.inSingle(argument) + ", which can take several minutes.");
                addHost(HostBuilder.withIdentifier(HostIdentifier.with(argument)).build());
            } else {
                Console.writeLine(Quotes.inSingle(argument) + " is not a valid host identifier!");
                shutDown();
            }
        }
        
        Options.start();
    }
    
}
