package ch.virtualid.module;

import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.entity.Site;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.server.Host;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
     * Maps the modules that are used on both hosts and clients from their type.
     */
    private static final Map<SemanticType, BothModule> bothModules = new HashMap<SemanticType, BothModule>();
    
    /**
     * Returns the both module with the given type.
     * 
     * @param type the type of the module to return.
     * 
     * @return the both module with the given type.
     */
    @Pure
    public static @Nullable BothModule get(@Nonnull SemanticType type) {
        return bothModules.get(type);
    }
    
    /**
     * Adds the given both module to both the list of host and client modules.
     * 
     * @param bothModule the module to add to both the list of host and client modules.
     */
    protected static void add(@Nonnull BothModule bothModule) {
        hostModules.add(bothModule);
        clientModules.add(bothModule);
        bothModules.put(bothModule.getType(), bothModule);
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
