package net.digitalid.core.server;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.collections.collection.ReadOnlyCollection;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.console.Console;
import net.digitalid.utility.console.Option;
import net.digitalid.utility.console.exceptions.EscapeOptionException;
import net.digitalid.utility.logging.Version;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.Committing;

import net.digitalid.core.host.Host;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.service.Service;

/**
 * This class contains the command-line options of the {@link Server}.
 * 
 * @see Option
 * @see Console
 */
@Utility
abstract class Options {
    
    /**
     * Starts the console with the command-line options.
     */
    @Impure
    static void start() {
        Console.writeLine();
        Console.options.add(new ExitServer());
        Console.options.add(new ShowVersion());
        Console.options.add(new ShowHosts());
        Console.options.add(new CreateHost());
        Console.options.add(new ExportHost());
        Console.options.add(new ImportHost());
        Console.options.add(new ShowServices());
        Console.options.add(new LoadServices());
        Console.options.add(new ActivateService());
        Console.options.add(new DeactivateService());
        Console.options.add(new ChangeProvider());
        Console.options.add(new GenerateTokens());
        Console.options.add(new ShowMembers());
        Console.options.add(new AddMembers());
        Console.options.add(new RemoveMembers());
        Console.options.add(new OpenHost());
        Console.options.add(new CloseHost());
        Console.start();
    }
    
    /**
     * Prompts the user to select one of the hosts that run on the server.
     * 
     * @return the selected host or a {@link EscapeOptionException} if the user escaped.
     */
    @Impure
    private static @Nonnull Host selectHost() throws EscapeOptionException {
        final @Nonnull ReadOnlyList<Host> hosts = FreezableArrayList.withElementsOf((Collection<? extends Host>) Server.getHosts()).freeze();
        if (!hosts.isEmpty()) {
            Console.writeLine("Please select one of the following hosts:");
            Console.writeLine("- 0: [Escape]");
            int i = 1;
            for (final @Nonnull Host host : hosts) {
                Console.writeLine("- " + (i++) + ": " + host.getIdentifier());
            }
            Console.writeLine();
            final int input = Console.readNumber("Choice: ", 0) - 1;
            Console.writeLine();
            if (input >= 0 && input < hosts.size()) {
                return hosts.get(input);
            } else if (input > 0) {
                Console.writeLine("Please choose one of the given options!");
            }
        } else {
            Console.writeLine("There are no hosts to proceed with.");
        }
        throw new EscapeOptionException();
    }
    
    
    /**
     * This option exits the server.
     */
    private static final class ExitServer extends Option {
        
        ExitServer() { super("Exit the server."); }
        
        @Override
        public void execute() {
            // TODO: Remove the true on the following line!
            if (true || Console.readBoolean("Are you sure you want to shut down the server? Yes or no (the default is no): ", false)) {
                Console.writeLine("The server is shutting down...");
                Console.writeLine();
                Server.shutDown();
            }
        }
        
    }
    
    /**
     * This option shows the version and the authors.
     */
    private static final class ShowVersion extends Option {
        
        ShowVersion() { super("Show the version."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("Version: " + Version.string.get());
        }
        
    }
    
    /**
     * This option shows the hosts.
     */
    private static final class ShowHosts extends Option {
        
        ShowHosts() { super("Show the hosts."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("The following hosts are running on this server:");
            for (final @Nonnull Host host : Server.getHosts()) {
                Console.writeLine("- " + host.getIdentifier().getString());
            }
            if (Server.getHosts().isEmpty()) { Console.writeLine("(None)"); }
        }
        
    }
    
    /**
     * This option adds a new host.
     */
    private static final class CreateHost extends Option {
        
        CreateHost() { super("Create a host."); }
        
        @Override
        @Committing
        public void execute() {
            if (Console.readBoolean("Are you sure you want to add a new host? Yes or no (the default is no): ", false)) {
                @Nonnull String string = Console.readString("Please enter the identifier of the new host: ", null);
                while (!HostIdentifier.isValid(string)) {
                    string = Console.readString("Bad input! Please enter a valid host identifier: ", null);
                }
                final @Nonnull HostIdentifier identifier = HostIdentifier.with(string);
                // TODO
//                try {
//                    new Host(identifier);
//                } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//                    Console.writeLine("Could not create the host " + identifier + " (" + exception + ").");
//                    Database.rollback();
//                }
            }
        }
        
    }
    
    /**
     * This option exports a host.
     */
    private static final class ExportHost extends Option {
        
        ExportHost() { super("Export a host."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option imports a host.
     */
    private static final class ImportHost extends Option {
        
        ImportHost() { super("Import a host."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option shows the installed services.
     */
    private static final class ShowServices extends Option {
        
        ShowServices() { super("Show the services."); }
        
        @Override
        @Committing
        public void execute() {
            final @Nonnull ReadOnlyCollection<Service> services = Service.getServices();
            Console.writeLine("The following services are installed on this server:");
            for (final @Nonnull Service service : services) {
                Console.writeLine("- " + service.getTitleWithVersion());
            }
            if (services.isEmpty()) { Console.writeLine("(None)"); }
        }
        
    }
    
    /**
     * This option reloads the available services.
     */
    private static final class LoadServices extends Option {
        
        LoadServices() { super("Reload the services."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("The services are reloaded.");
            Server.loadServices();
        }
        
    }
    
    /**
     * This option activates a service.
     */
    private static final class ActivateService extends Option {
        
        ActivateService() { super("Activate a service."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option deactivates a service.
     */
    private static final class DeactivateService extends Option {
        
        DeactivateService() { super("Deactivate a service."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option changes a provider.
     */
    private static final class ChangeProvider extends Option {
        
        ChangeProvider() { super("Change a provider."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option generates tokens.
     */
    private static final class GenerateTokens extends Option {
        
        GenerateTokens() { super("Generate tokens."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option shows members.
     */
    private static final class ShowMembers extends Option {
        
        ShowMembers() { super("Show members."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option adds members.
     */
    private static final class AddMembers extends Option {
        
        AddMembers() { super("Add members."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option removes members.
     */
    private static final class RemoveMembers extends Option {
        
        RemoveMembers() { super("Remove members."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option opens a host.
     */
    private static final class OpenHost extends Option {
        
        OpenHost() { super("Open a host."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option closes a host.
     */
    private static final class CloseHost extends Option {
        
        CloseHost() { super("Close a host."); }
        
        @Override
        @Committing
        public void execute() {
            Console.writeLine("To be implemented."); // TODO
        }
        
    }
    
}
