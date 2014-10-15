package ch.virtualid.module;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Site;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.server.Host;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableHashMap;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.FreezableMap;
import ch.virtualid.util.ReadonlyArray;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.TupleWrapper;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Every service has to extend this class.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Service implements BothModule {
    
    /**
     * Stores a list of the services installed on this server.
     */
    private static final @Nonnull FreezableList<Service> services = new FreezableLinkedList<Service>();
    
    /**
     * Returns a list of the services installed on this server.
     * 
     * @return a list of the services installed on this server.
     */
    public static @Nonnull ReadonlyList<Service> getServices() {
        return services;
    }
    
    
    /**
     * Stores the name of this service.
     */
    private final @Nonnull String name;
    
    /**
     * Stores the version of this service.
     */
    private final @Nonnull String version;
    
    /**
     * Creates a new service with the given name and version.
     * 
     * @param name the name of the service.
     * @param version the version of the service.
     */
    protected Service(@Nonnull String name, @Nonnull String version) {
        this.name = name;
        this.version = version;
        services.add(this);
    }
    
    /**
     * Returns the name of this service.
     * 
     * @return the name of this service.
     */
    @Pure
    public final @Nonnull String getName() {
        return name;
    }
    
    /**
     * Returns the version of this service.
     * 
     * @return the version of this service.
     */
    @Pure
    public final @Nonnull String getVersion() {
        return version;
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return name + " (" + version + ")";
    }
    
    
    @Override
    protected final void createTables(@Nonnull Site site) throws SQLException {
        throw new ShouldNeverHappenError("The method 'createTables' should never be called on a service.");
    }
    
    
    /**
     * Stores the modules that represent an entity's state in the specified order.
     */
    private final @Nonnull FreezableList<BothModule> modules = new FreezableLinkedList<BothModule>();
    
    /**
     * Returns the list of both modules that belong to this service.
     * 
     * @return the list of both modules that belong to this service.
     */
    public final @Nonnull ReadonlyList<BothModule> getModules() {
        return modules;
    }
    
    /**
     * Adds the given module to the tuple of modules.
     * 
     * @param module the module to add to the tuple of modules.
     */
    protected final void addToTuple(@Nonnull BothModule module) {
        modules.add(module);
    }
    
    @Pure
    @Override
    public final @Nonnull Block getState(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        final int size = modules.size();
        final @Nonnull FreezableArray<Block> blocks = new FreezableArray<Block>(size);
        for (int i = 0; i < size; i++) blocks.set(i, modules.get(i).getState(entity, agent));
        return new TupleWrapper(getStateFormat(), blocks.freeze()).toBlock();
    }
    
    @Override
    public final void addState(@Nonnull Entity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final int size = modules.size();
        final @Nonnull ReadonlyArray<Block> blocks = new TupleWrapper(block).getElementsNotNull(size);
        for (int i = 0; i < size; i++) modules.get(i).addState(entity, blocks.getNotNull(i));
    }
    
    @Override
    public final void removeState(@Nonnull Entity entity) throws SQLException {
      for (final @Nonnull BothModule module : modules) module.removeState(entity);
    }
    
    
    
    /**
     * Stores all the modules that are used on hosts.
     */
    private static final FreezableList<Module> hostModules = new FreezableLinkedList<Module>();
    
    /**
     * Stores all the modules that are used on clients.
     */
    private static final FreezableList<Module> clientModules = new FreezableLinkedList<Module>();
    
    /**
     * Maps the modules that are used on both hosts and clients from their type.
     */
    private static final FreezableMap<SemanticType, BothModule> bothModules = new FreezableHashMap<SemanticType, BothModule>();
    
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
        bothModules.put(bothModule.getStateFormat(), bothModule);
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
    
    
    
}
