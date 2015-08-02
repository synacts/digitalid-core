package net.digitalid.core.service;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.attribute.Attribute;
import net.digitalid.core.attribute.AttributeValue;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.client.Client;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableLinkedHashMap;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.FreezableMap;
import net.digitalid.core.collections.ReadOnlyCollection;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identity.IdentityClass;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.database.SQLizable;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.module.ClientModule;
import net.digitalid.core.module.HostModule;
import net.digitalid.core.module.Module;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.SelfcontainedWrapper;

/**
 * Every service has to extend this class.
 * 
 * @see CoreService
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class Service implements BothModule, SQLizable {
    
    /**
     * Maps the services that are installed on this server from their type.
     */
    private static final @Nonnull FreezableMap<SemanticType, Service> services = new FreezableLinkedHashMap<>();
    
    /**
     * Returns a list of the services installed on this server.
     * 
     * @return a list of the services installed on this server.
     */
    @Pure
    public static @Nonnull ReadOnlyCollection<Service> getServices() {
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
    private static final @Nonnull FreezableMap<SemanticType, BothModule> modules = new FreezableLinkedHashMap<>();
    
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
    @NonCommitting
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
    @NonCommitting
    public @Nonnull HostIdentifier getRecipient(@Nullable Role role, @Nonnull InternalPerson subject) throws SQLException, IOException, PacketException, ExternalException {
        return IdentifierClass.create(Cache.getFreshAttributeContent(subject, role, getType(), false)).toHostIdentifier();
    }
    
    
    /**
     * Maps the modules that are used on hosts from their module format.
     */
    private final @Nonnull FreezableMap<SemanticType, HostModule> hostModules = new FreezableLinkedHashMap<>();
    
    /**
     * Stores the modules of this service that are used on clients.
     */
    private final @Nonnull FreezableList<Module> clientModules = new FreezableLinkedList<>();
    
    /**
     * Maps the modules that are used on both hosts and clients from their state format.
     */
    private final @Nonnull FreezableMap<SemanticType, BothModule> bothModules = new FreezableLinkedHashMap<>();
    
    /**
     * Returns the modules that are used on both hosts and clients of this service.
     * 
     * @return the modules that are used on both hosts and clients of this service.
     */
    @Pure
    public final @Nonnull ReadOnlyCollection<BothModule> getBothModules() {
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
    @NonCommitting
    public final void createTables(@Nonnull Site site) throws SQLException {
        if (site instanceof Host) {
            for (final @Nonnull Module module : hostModules.values()) module.createTables(site);
        } else if (site instanceof Client) {
            for (final @Nonnull Module module : clientModules) module.createTables(site);
        }
    }
    
    @Override
    @NonCommitting
    public final void deleteTables(@Nonnull Site site) throws SQLException {
        if (site instanceof Host) {
            for (final @Nonnull Module module : hostModules.values()) module.deleteTables(site);
        } else if (site instanceof Client) {
            for (final @Nonnull Module module : clientModules) module.deleteTables(site);
        }
    }
    
    
    /**
     * Stores the semantic type {@code module.service@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.map("module.service@core.digitalid.net").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code list.module.service@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType MODULES = SemanticType.map("list.module.service@core.digitalid.net").load(ListWrapper.TYPE, MODULE);
    
    /**
     * @ensure return.isBasedOn(MODULES) : "The returned type is based on the modules format.";
     */
    @Pure
    @Override
    public abstract @Nonnull SemanticType getModuleFormat();
    
    @Override
    @NonCommitting
    public final @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<>(hostModules.size());
        for (final @Nonnull HostModule hostModule : hostModules.values()) {
            elements.add(new SelfcontainedWrapper(MODULE, hostModule.exportModule(host)).toBlock());
        }
        return new ListWrapper(getModuleFormat(), elements.freeze()).toBlock();
    }
    
    @Override
    @NonCommitting
    public final void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadOnlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block module = new SelfcontainedWrapper(element).getElement();
            final @Nullable HostModule hostModule = hostModules.get(module.getType());
            if (hostModule != null) hostModule.importModule(host, module);
            else throw new InvalidEncodingException("There is no module for the block of type " + module.getType().getAddress() + ".");
        }
    }
    
    
    /**
     * Stores the semantic type {@code state.service@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.map("state.service@core.digitalid.net").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code list.state.service@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType STATES = SemanticType.map("list.state.service@core.digitalid.net").load(ListWrapper.TYPE, STATE);
    
    /**
     * @ensure return.isBasedOn(STATES) : "The returned type is based on the states format.";
     */
    @Pure
    @Override
    public abstract @Nonnull SemanticType getStateFormat();
    
    @Pure
    @Override
    @NonCommitting
    public final @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        final @Nonnull FreezableList<Block> elements = new FreezableArrayList<>(bothModules.size());
        for (final @Nonnull BothModule bothModule : bothModules.values()) {
            elements.add(new SelfcontainedWrapper(STATE, bothModule.getState(entity, permissions, restrictions, agent)).toBlock());
        }
        return new ListWrapper(getStateFormat(), elements.freeze()).toBlock();
    }
    
    @Override
    @NonCommitting
    public final void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyList<Block> elements = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block state = new SelfcontainedWrapper(element).getElement();
            final @Nullable BothModule bothModule = bothModules.get(state.getType());
            if (bothModule != null) bothModule.addState(entity, state);
            else throw new InvalidEncodingException("There is no module for the state of type " + state.getType().getAddress() + ".");
        }
    }
    
    @Override
    @NonCommitting
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
    @NonCommitting
    public static @Nonnull Service get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException, PacketException, InvalidEncodingException {
        return getService(IdentityClass.getNotNull(resultSet, columnIndex).toSemanticType());
    }
    
    @Override
    @NonCommitting
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
        return super.equals(object);
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return super.hashCode();
    }
    
}
