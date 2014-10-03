package ch.virtualid.server;

import ch.virtualid.client.Client;
import ch.virtualid.database.Database;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.io.Console;
import ch.virtualid.io.Directory;
import ch.virtualid.io.Option;
import ch.virtualid.module.CoreService;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The server runs the configured hosts.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class Server {
    
    /**
     * The version of the Virtual ID server implementation.
     */
    public static final @Nonnull String VERSION = "0.8 (3 October 2014)";
    
    /**
     * The authors of the Virtual ID server implementation.
     */
    public static final @Nonnull String AUTHORS = "Kaspar Etter (kaspar.etter@virtualid.ch)";
    
    /**
     * The server listens on the given port number.
     */
    public static final int PORT = 1988;
    
    
    /**
     * Reference to the thread that listens on the socket.
     */
    private static final @Nonnull Listener listener = new Listener();
    
    /**
     * Maps the identifiers of the hosts that are running on this server to their instances.
     */
    private static final @Nonnull Map<HostIdentifier, Host> hosts = new HashMap<HostIdentifier, Host>();
    
    /**
     * Returns whether the host with the given identifier is running on this server.
     * 
     * @param hostIdentifier the identifier of the host of interest.
     * @return whether the host with the given identifier is running on this server.
     */
    public static boolean hasHost(@Nonnull HostIdentifier hostIdentifier) {
        return hosts.containsKey(hostIdentifier);
    }
    
    /**
     * Returns the host with the given identifier that is running on this server.
     * 
     * @param hostIdentifier the identifier of the host of interest.
     * @return the host with the given identifier that is running on this server.
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
     */
    public static @Nonnull Collection<Host> getHosts() {
        return hosts.values();
    }
    
    /**
     * Adds the given host to the map of running hosts.
     * 
     * @param host the host to add.
     */
    static void addHost(@Nonnull Host host) {
        hosts.put(host.getIdentifier(), host);
    }
    
    /**
     * Loads all hosts with a configuration in the hosts directory.
     */
    private static void loadHosts() {
        @Nonnull File[] files = Directory.HOSTS.listFiles();
        for (@Nonnull File file : files) {
            if (!file.isDirectory() && file.getName().endsWith(".private.xdf")) {
                try {
                    new Host(new HostIdentifier(file.getName().substring(0, file.getName().length() - 12)));
                } catch (@Nonnull InvalidEncodingException | SQLException | IOException | FailedEncodingException exception) {
                    throw new InitializationError("Could not load the host configured in the file '" + file.getName() + "'.", exception);
                }
            }
        }
    }
    
    
    /**
     * A list of the installed services (including the version numbers).
     */
    private static final @Nonnull List<String> services = new LinkedList<String>();
    
    /**
     * Loads all services with their code in the services directory.
     */
    private static void loadServices() {
        services.clear();
        @Nonnull File[] files = Directory.SERVICES.listFiles();
        for (@Nonnull File file : files) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try {
                    @Nonnull JarFile jarFile = new JarFile(file);
                    @Nullable Manifest manifest = jarFile.getManifest();
                    if (manifest == null) throw new IOException("Could not find the manifest of '" + file.getName() + "'.");
                    @Nonnull Attributes attributes = manifest.getMainAttributes();
                    @Nonnull URLClassLoader classLoader = new URLClassLoader(new URL[] {file.toURI().toURL()});
                    @Nullable String mainClass = attributes.getValue("Main-Class");
                    // TODO: mainClass can be null!
                    @Nonnull Class<?> service = classLoader.loadClass(mainClass);
                    @Nonnull Method method = service.getDeclaredMethod("initialize");
                    // TODO: Call default constructor instead!
                    method.invoke(null);
                } catch (@Nonnull IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
                    // TODO: Fail more gracefully (e.g. by returning a list of services that could not be loaded)!
                    throw new InitializationError("Could not load the service in the file '" + file.getName() + "'.", exception);
                }
            }
        }
    }
    
    /**
     * Adds the given service to the list of installed services.
     * 
     * @param service the service to be added.
     */
    public static void addService(@Nonnull String service) {
        services.add(service);
    }
    
    
    /**
     * Starts the server with the configured and given hosts.
     * 
     * @param arguments the identifiers of hosts to be created when starting up.
     */
    public static void start(@Nonnull String[] arguments) {
        CoreService.initialize();
        
        loadHosts();
        loadServices();
        
        for (@Nonnull String argument : arguments) {
            try {
                new Host(new HostIdentifier(argument));
            } catch (@Nonnull InvalidEncodingException | SQLException | IOException | FailedEncodingException exception) {
                throw new InitializationError("Could not create the host '" + argument + "'.", exception);
            }
        }
        
        listener.start();
        
        try { Client.getAttributeNotNull(HostIdentity.VIRTUALID, SemanticType.HOST_PUBLIC_KEY); } catch (@Nonnull InvalidEncodingException exception) { throw new InitializationError("Could not retrieve the public key of 'virtualid.ch'.", exception); }
    }
    
    /**
     * The main method starts the server with the configured hosts and shows the console.
     * 
     * @param arguments the command line arguments indicating the hosts to be created when starting up.
     */
    public static void main(@Nonnull String[] arguments) {
        Database.initializeForMySQL();
        
        start(arguments);
        
        Console.write();
        Console.addOption(new ShowVersion());
        Console.addOption(new ExitServer());
        Console.addOption(new ShowHosts());
        Console.addOption(new CreateHost());
        Console.addOption(new ShowServices());
        Console.addOption(new ReloadServices());
        Console.start();
    }
    
    /**
     * Shuts down the server after having handled all pending requests.
     */
    public static void shutDown() {
        listener.shutDown();
        System.exit(0);
    }
    
    
    /**
     * This option exits the server.
     */
    private static final class ExitServer extends Option {
        
        ExitServer() { super("Exit the server."); }
        
        @Override
        public void execute() {
            if (Console.readBoolean("Are you sure you want to shut down the server? Yes/No: ")) {
                Console.write("The server is shutting down...");
                Console.write();
                shutDown();
            }
        }
        
    }
    
    /**
     * This option shows the version and the authors.
     */
    private static final class ShowVersion extends Option {
        
        ShowVersion() { super("Show the version."); }
        
        @Override
        public void execute() {
            Console.write("Version: " + VERSION);
            Console.write("Authors: " + AUTHORS);
        }
        
    }
    
    /**
     * This option shows the hosts.
     */
    private static final class ShowHosts extends Option {
        
        ShowHosts() { super("Show the hosts."); }
        
        @Override
        public void execute() {
            Console.write("The following hosts are running on this server:");
            for (@Nonnull Host host : hosts.values()) {
                Console.write("- " + host.getIdentifier());
            }
            if (hosts.values().isEmpty()) Console.write("(None)");
        }
        
    }
    
    /**
     * This option adds a new host.
     */
    private static final class CreateHost extends Option {
        
        CreateHost() { super("Create a host."); }
        
        @Override
        public void execute() {
            if (Console.readBoolean("Are you sure you want to add a new host? Yes/No: ")) {
                @Nonnull String string = Console.readString("Please enter the identifier of the new host: ");
                while (!Identifier.isValid(string) || !Identifier.isHost(string)) {
                    string = Console.readString("Bad input! Please enter a valid host identifier: ");
                }
                @Nonnull HostIdentifier identifier = new HostIdentifier(string, false);
                try {
                    new Host(identifier);
                } catch (@Nonnull SQLException | IOException | InvalidEncodingException | FailedEncodingException exception) {
                    Console.write("Could not create the host " + identifier + " (" + exception + ").");
                }
            }
        }
        
    }
    
    /**
     * This option shows the installed services.
     */
    private static final class ShowServices extends Option {
        
        ShowServices() { super("Show the services."); }
        
        @Override
        public void execute() {
            Console.write("The following services are installed on this server:");
            for (@Nonnull String service : services) {
                Console.write("- " + service);
            }
            if (services.isEmpty()) Console.write("(None)");
        }
        
    }
    
    /**
     * This option reloads the available services.
     */
    private static final class ReloadServices extends Option {
        
        ReloadServices() { super("Reload the services."); }
        
        @Override
        public void execute() {
            Console.write("The services are reloaded.");
            loadServices();
        }
        
    }
    
}
