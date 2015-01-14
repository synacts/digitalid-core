package ch.virtualid.service;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.attribute.Attribute;
import ch.virtualid.attribute.AttributeValue;
import ch.virtualid.cache.Cache;
import ch.virtualid.client.Client;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.ClientModule;
import ch.virtualid.module.HostModule;
import ch.virtualid.module.Module;
import ch.virtualid.host.Host;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableLinkedHashMap;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.FreezableMap;
import ch.virtualid.util.ReadonlyCollection;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.SelfcontainedWrapper;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
public abstract class Service implements BothModule, SQLizable {
    
    /**
     * Maps the services that are installed on this server from their type.
     */
    private static final @Nonnull FreezableMap<SemanticType, Service> services = new FreezableLinkedHashMap<SemanticType, Service>();
    
    /**
     * Returns a list of the services installed on this server.
     * 
     * @return a list of the services installed on this server.
     */
    @Pure
    public static @Nonnull ReadonlyCollection<Service> getServices() {
        return services.values();
    }
    
    /**
     * Returns the service with the given type.
     * 
     * @param type the type of the desired service.
     * 
     * @return the service with the given type.
     */
    @Pure
    public static @Nonnull Service getService(@Nonnull SemanticType type) throws PacketException {
        final @Nullable Service service = services.get(type);
        if (service != null) return service;
        throw new PacketException(PacketError.SERVICE, "No service with the type " + type.getAddress() + " is installed.");
    }
    
    
    /**
     * Maps the modules that exist on this server from their state format.
     */
    private static final @Nonnull FreezableMap<SemanticType, BothModule> modules = new FreezableLinkedHashMap<SemanticType, BothModule>();
    
    /**
     * Returns the module whose state format matches the given type.
     * 
     * @param stateFormat the state format of the desired module.
     * 
     * @return the module whose state format matches the given type.
     */
    @Pure
    public static @Nonnull BothModule getModule(@Nonnull SemanticType stateFormat) throws PacketException {
        final @Nullable BothModule module = modules.get(stateFormat);
        if (module != null) return module;
        throw new PacketException(PacketError.SERVICE, "There exists no module with the state format " + stateFormat.getAddress() + ".");
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
        services.put(getType(), this);
        modules.put(getStateFormat(), this);
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
    
    /**
     * Returns the name with the version of this service.
     * 
     * @return the name with the version of this service.
     */
    @Pure
    public final @Nonnull String getNameWithVersion() {
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
     * Returns the recipient of internal methods for the given role.
     * 
     * @param role the role for which the recipient is to be returned.
     * 
     * @return the recipient of internal methods for the given role.
     */
    @Pure
    public @Nonnull HostIdentifier getRecipient(@Nonnull Role role) throws SQLException, PacketException, InvalidEncodingException {
        final @Nullable AttributeValue attributeValue = Attribute.get(role, getType()).getValue();
        if (attributeValue == null) throw new PacketException(PacketError.AUTHORIZATION, "Could not read the attribute value of " + getType().getAddress() + ".");
        return IdentifierClass.create(attributeValue.getContent()).toHostIdentifier();
    }
    
    /**
     * Returns the recipient of external methods for the given subject.
     * 
     * @param role the role that sends the method or null for hosts.
     * @param subject the subject for which the recipient is to be returned.
     * 
     * @return the recipient of external methods for the given subject.
     */
    @Pure
    public @Nonnull HostIdentifier getRecipient(@Nullable Role role, @Nonnull InternalNonHostIdentity subject) throws SQLException, IOException, PacketException, ExternalException {
        return IdentifierClass.create(Cache.getFreshAttributeContent(subject, role, getType(), false)).toHostIdentifier();
    }
    
    
    /**
     * Maps the modules that are used on hosts from their module format.
     */
    private final @Nonnull FreezableMap<SemanticType, HostModule> hostModules = new FreezableLinkedHashMap<SemanticType, HostModule>();
    
    /**
     * Stores the modules of this service that are used on clients.
     */
    private final @Nonnull FreezableList<Module> clientModules = new FreezableLinkedList<Module>();
    
    /**
     * Maps the modules that are used on both hosts and clients from their state format.
     */
    private final @Nonnull FreezableMap<SemanticType, BothModule> bothModules = new FreezableLinkedHashMap<SemanticType, BothModule>();
    
    /**
     * Returns the modules that are used on both hosts and clients of this service.
     * 
     * @return the modules that are used on both hosts and clients of this service.
     */
    @Pure
    public final @Nonnull ReadonlyCollection<BothModule> getBothModules() {
        return bothModules.values();
    }
    
    /**
     * Adds the given both module to the list of host, client and both modules.
     * 
     * @param bothModule the module to add to the list of host, client and both modules.
     */
    public final void add(@Nonnull BothModule bothModule) {
        hostModules.put(bothModule.getModuleFormat(), bothModule);
        clientModules.add(bothModule);
        bothModules.put(bothModule.getStateFormat(), bothModule);
        modules.put(bothModule.getStateFormat(), bothModule);
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
            final @Nonnull Block module = new SelfcontainedWrapper(element).getElement();
            final @Nullable HostModule hostModule = hostModules.get(module.getType());
            if (hostModule != null) hostModule.importModule(host, module);
            else throw new InvalidEncodingException("There is no module for the block of type " + module.getType().getAddress() + ".");
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
    public final @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadonlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<Block>(bothModules.size());
        for (final @Nonnull BothModule bothModule : bothModules.values()) {
            elements.add(new SelfcontainedWrapper(STATE, bothModule.getState(entity, permissions, restrictions, agent)).toBlock());
        }
        return new ListWrapper(getStateFormat(), elements.freeze()).toBlock();
    }
    
    @Override
    public final void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block state = new SelfcontainedWrapper(element).getElement();
            final @Nullable BothModule bothModule = bothModules.get(state.getType());
            if (bothModule != null) bothModule.addState(entity, state);
            else throw new InvalidEncodingException("There is no module for the state of type " + state.getType().getAddress() + ".");
        }
    }
    
    @Override
    public final void removeState(@Nonnull NonHostEntity entity) throws SQLException {
      for (final @Nonnull BothModule bothModule : bothModules.values()) bothModule.removeState(entity);
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = Mapper.FORMAT;
    
    /**
     * Stores the foreign key constraint used to reference instances of this class.
     */
    public static final @Nonnull String REFERENCE = Mapper.REFERENCE;
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Service get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException, PacketException, InvalidEncodingException {
        return getService(IdentityClass.getNotNull(resultSet, columnIndex).toSemanticType());
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        getType().set(preparedStatement, parameterIndex);
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return getType().toString();
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
