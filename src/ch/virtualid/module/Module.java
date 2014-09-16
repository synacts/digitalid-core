package ch.virtualid.module;

import ch.virtualid.client.Client;
import ch.virtualid.entity.Site;
import ch.virtualid.server.Host;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A module manages an {@link Entity entity}'s partial state in the {@link Database database}.
 * 
 * @see BothModule
 * @see HostModule
 * @see ClientModule
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Module {
    
    /**
     * Stores all the modules that are used on hosts.
     */
    private static final List<Module> hostModules = new LinkedList<Module>();
    
    /**
     * Stores all the modules that are used on clients.
     */
    private static final List<Module> clientModules = new LinkedList<Module>();
    
    /**
     * Adds the given both module to both the list of host and client modules.
     * 
     * @param bothModule the module to add to both the list of host and client modules.
     */
    protected static void add(@Nonnull BothModule bothModule) {
        hostModules.add(bothModule);
        clientModules.add(bothModule);
    }
    
    /**
     * Adds the given host module to the list of host modules.
     * 
     * @param hostModule the module to add to the list of host modules.
     */
    protected static void add(@Nonnull HostModule hostModule) {
        hostModules.add(hostModule);
    }
    
    /**
     * Adds the given client module to the list of client modules.
     * 
     * @param clientModule the module to add to the list of client modules.
     */
    protected static void add(@Nonnull ClientModule clientModule) {
        clientModules.add(clientModule);
    }
    
    /**
     * Initializes the database tables for the given site.
     * 
     * @param site the site for which to initialize the database tables.
     */
    public static void initialize(@Nonnull Site site) throws SQLException {
        if (site instanceof Host) {
            for (final @Nonnull Module module : hostModules) module.createTables(site);
        } else if (site instanceof Client) {
            for (final @Nonnull Module module : clientModules) module.createTables(site);
        }
    }
    
    
    /**
     * Creates the database tables for the given site.
     * 
     * @param site the site for which to create the database tables.
     */
    protected abstract void createTables(@Nonnull Site site) throws SQLException;
    
}
