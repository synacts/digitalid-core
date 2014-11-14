package ch.virtualid.module;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.server.Host;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableLinkedHashMap;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.FreezableMap;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.SelfcontainedWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Every service has to extend this class.
 * 
 * @see CoreService
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
    
    
    /**
     * Returns the type of this service.
     * 
     * @return the type of this service.
     */
    @Pure
    public abstract @Nonnull SemanticType getType();
    
    
    /**
     * Maps the modules that are used on hosts from their module format.
     */
    private final FreezableMap<SemanticType, HostModule> hostModules = new FreezableLinkedHashMap<SemanticType, HostModule>();
    
    /**
     * Stores the modules of this service that are used on clients.
     */
    private final FreezableList<Module> clientModules = new FreezableLinkedList<Module>();
    
    /**
     * Maps the modules that are used on both hosts and clients from their state format.
     */
    private final FreezableMap<SemanticType, BothModule> bothModules = new FreezableLinkedHashMap<SemanticType, BothModule>();
    
    /**
     * Adds the given both module to the list of host, client and both modules.
     * 
     * @param bothModule the module to add to the list of host, client and both modules.
     */
    public final void add(@Nonnull BothModule bothModule) {
        hostModules.put(bothModule.getModuleFormat(), bothModule);
        clientModules.add(bothModule);
        bothModules.put(bothModule.getStateFormat(), bothModule);
    }
    
    /**
     * Adds the given host module to the list of host modules.
     * 
     * @param hostModule the module to add to the list of host modules.
     */
    public final void add(@Nonnull HostModule hostModule) {
        hostModules.put(hostModule.getModuleFormat(), hostModule);
    }
    
    /**
     * Adds the given client module to the list of client modules.
     * 
     * @param clientModule the module to add to the list of client modules.
     */
    public final void add(@Nonnull ClientModule clientModule) {
        clientModules.add(clientModule);
    }
    
    
    @Override
    public final void createTables(@Nonnull Site site) throws SQLException {
        if (site instanceof Host) {
            for (final @Nonnull Module module : hostModules.values()) module.createTables(site);
        } else if (site instanceof Client) {
            for (final @Nonnull Module module : clientModules) module.createTables(site);
        }
    }
    
    @Override
    public final void deleteTables(@Nonnull Site site) throws SQLException {
        if (site instanceof Host) {
            for (final @Nonnull Module module : hostModules.values()) module.deleteTables(site);
        } else if (site instanceof Client) {
            for (final @Nonnull Module module : clientModules) module.deleteTables(site);
        }
    }
    
    
    /**
     * Stores the semantic type {@code module.service@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.create("module.service@virtualid.ch").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code list.module.service@virtualid.ch}.
     */
    public static final @Nonnull SemanticType MODULES = SemanticType.create("list.module.service@virtualid.ch").load(ListWrapper.TYPE, MODULE);
    
    /**
     * @ensure return.isBasedOn(MODULES) : "The returned type is based on the modules format.";
     */
    @Pure
    @Override
    public abstract @Nonnull SemanticType getModuleFormat();
    
    @Override
    public final @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<Block>(hostModules.size());
        for (final @Nonnull HostModule hostModule : hostModules.values()) {
            elements.add(new SelfcontainedWrapper(MODULE, hostModule.exportModule(host)).toBlock());
        }
        return new ListWrapper(getModuleFormat(), elements.freeze()).toBlock();
    }
    
    @Override
    public final void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block module = new SelfcontainedWrapper(element).toBlock();
            hostModules.get(module.getType()).importModule(host, module);
        }
    }
    
    
    /**
     * Stores the semantic type {@code state.service@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.create("state.service@virtualid.ch").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code list.state.service@virtualid.ch}.
     */
    public static final @Nonnull SemanticType STATES = SemanticType.create("list.state.service@virtualid.ch").load(ListWrapper.TYPE, STATE);
    
    /**
     * @ensure return.isBasedOn(STATES) : "The returned type is based on the states format.";
     */
    @Pure
    @Override
    public abstract @Nonnull SemanticType getStateFormat();
    
    @Pure
    @Override
    public final @Nonnull Block getState(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<Block>(bothModules.size());
        for (final @Nonnull BothModule bothModule : bothModules.values()) {
            elements.add(new SelfcontainedWrapper(STATE, bothModule.getState(entity, agent)).toBlock());
        }
        return new ListWrapper(getStateFormat(), elements.freeze()).toBlock();
    }
    
    @Override
    public final void addState(@Nonnull Entity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block state = new SelfcontainedWrapper(element).toBlock();
            bothModules.get(state.getType()).addState(entity, state);
        }
    }
    
    @Override
    public final void removeState(@Nonnull Entity entity) throws SQLException {
      for (final @Nonnull BothModule bothModule : bothModules.values()) bothModule.removeState(entity);
    }
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        return object == this;
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return getType().hashCode();
    }
    
}
