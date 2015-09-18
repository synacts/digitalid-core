package net.digitalid.core.data;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.OnMainThread;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.client.Client;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableLinkedHashMap;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.FreezableMap;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.SelfcontainedWrapper;

/**
 * State modules are used on both {@link Host hosts} and {@link Client clients}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class StateModule extends HostModule implements StateData {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code table.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TABLE = SemanticType.map("table.state@core.digitalid.net").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.map("state@core.digitalid.net").load(ListWrapper.TYPE, TABLE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Modules –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Maps the modules that exist on this server from their state type.
     */
    private static final @Nonnull FreezableMap<SemanticType, StateModule> modules = FreezableLinkedHashMap.get();
    
    /**
     * Returns the module whose state type matches the given type.
     * 
     * @param stateType the state type of the desired module.
     * 
     * @return the module whose state type matches the given type.
     */
    @Pure
    public static @Nonnull StateModule getModule(@Nonnull SemanticType stateType) throws PacketException {
        final @Nullable StateModule module = modules.get(stateType);
        if (module != null) return module;
        throw new PacketException(PacketError.SERVICE, "There exists no module with the state type " + stateType.getAddress() + ".");
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the state type of this module.
     */
    private final @Nonnull @Loaded SemanticType stateType;
    
    @Pure
    @Override
    public final @Nonnull @Loaded SemanticType getStateType() {
        return stateType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new state module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     */
    @OnMainThread
    protected StateModule(@Nullable Service service, @Nonnull @Validated String name) {
        super(service, name);
        
        final @Nonnull String identifier;
        if (service == null) {
            final @Nonnull String address = ((Service) this).getType().getAddress().getString();
            identifier = "state.service" + (address.startsWith("@") ? "" : ".") + address;
        } else {
            identifier = "state." + name + service.getType().getAddress().getString();
            service.register(this);
        }
        this.stateType = SemanticType.map(identifier).load(STATE);
        
        modules.put(stateType, this);
    }
    
    /**
     * Returns a new state module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     * 
     * @return a new state module with the given service and name.
     */
    @Pure
    @OnMainThread
    public static @Nonnull StateModule get(@Nonnull Service service, @Nonnull @Validated String name) {
        return new StateModule(service, name);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Sites –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final boolean isForClients() {
        return true;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Tables –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the tables of this module.
     */
    private final @Nonnull @NonNullableElements @NonFrozen FreezableMap<SemanticType, StateData> tables = FreezableLinkedHashMap.get();
    
    /**
     * Registers the given table at this module.
     * 
     * @param table the table to be registered.
     */
    final void register(@Nonnull StateData table) {
        tables.put(table.getStateType(), table);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public final @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        final @Nonnull FreezableList<Block> elements = FreezableArrayList.getWithCapacity(tables.size());
        for (final @Nonnull StateData table : tables.values()) elements.add(SelfcontainedWrapper.encodeNonNullable(TABLE, table.getState(entity, permissions, restrictions, agent)));
        return ListWrapper.encode(stateType, elements.freeze());
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull @NonNullableElements ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block selfcontained = SelfcontainedWrapper.decodeNonNullable(element);
            final @Nullable StateData table = tables.get(selfcontained.getType());
            if (table == null) throw new InvalidEncodingException("There is no table for the block of type " + selfcontained.getType() + ".");
            table.addState(entity, selfcontained);
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void removeState(@Nonnull NonHostEntity entity) throws SQLException {
      for (final @Nonnull StateData table : tables.values()) table.removeState(entity);
    }
    
}
