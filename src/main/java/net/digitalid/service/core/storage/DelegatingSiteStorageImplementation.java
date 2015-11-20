package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.SelfcontainedWrapper;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashMap;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.freezable.FreezableMap;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.annotations.OnMainThread;
import net.digitalid.utility.database.exceptions.DatabaseException;

/**
 * This class implements a storage that delegates the retrieval of an {@link Entity entity's} state to substorages on {@link Host hosts} and {@link Client clients}.
 * 
 * @see SiteModule
 * @see Service
 */
@Immutable
abstract class DelegatingSiteStorageImplementation extends DelegatingHostStorageImplementation implements SiteStorage {
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code table.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TABLE = SemanticType.map("table.state@core.digitalid.net").load(SelfcontainedWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.map("state@core.digitalid.net").load(ListWrapper.XDF_TYPE, TABLE);
    
    /* -------------------------------------------------- State Type -------------------------------------------------- */
    
    /**
     * Stores the state type of this module.
     */
    private final @Nonnull @Loaded SemanticType stateType;
    
    @Pure
    @Override
    public final @Nonnull @Loaded SemanticType getStateType() {
        return stateType;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new site storage with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     */
    @OnMainThread
    protected DelegatingSiteStorageImplementation(@Nullable Service service, @Nonnull @Validated String name) {
        super(service, name);
        
        final @Nonnull String identifier;
        if (service == null) {
            identifier = "state.service" + ((Service) this).getType().getAddress().getStringWithDot();
        } else {
            identifier = "state." + name + service.getType().getAddress().getStringWithDot();
        }
        this.stateType = SemanticType.map(identifier).load(STATE);
        
        Storage.register(this);
    }
    
    /* -------------------------------------------------- Substorages -------------------------------------------------- */
    
    /**
     * Stores the substorages of this storage.
     */
    private final @Nonnull @NonNullableElements @NonFrozen FreezableMap<SemanticType, SiteStorage> substorages = FreezableLinkedHashMap.get();
    
    /**
     * Registers the given substorage at this storage.
     * 
     * @param substorage the substorage to be registered.
     */
    final void registerSiteStorage(@Nonnull SiteStorage substorage) {
        substorages.put(substorage.getStateType(), substorage);
        super.registerHostStorage(substorage);
    }
    
    /* -------------------------------------------------- State Methods -------------------------------------------------- */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public final @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException {
        final @Nonnull FreezableList<Block> elements = FreezableArrayList.getWithCapacity(substorages.size());
        for (final @Nonnull SiteStorage table : substorages.values()) { elements.add(SelfcontainedWrapper.encodeNonNullable(TABLE, table.getState(entity, permissions, restrictions, agent))); }
        return ListWrapper.encode(stateType, elements.freeze());
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws DatabaseException, PacketException, ExternalException, NetworkException {
        final @Nonnull @NonNullableElements ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block selfcontained = SelfcontainedWrapper.decodeNonNullable(element);
            final @Nullable SiteStorage substorage = substorages.get(selfcontained.getType());
            if (substorage == null) { throw new InvalidEncodingException("There is no table for the block of type " + selfcontained.getType() + "."); } // TODO: Change to a PacketException?
            substorage.addState(entity, selfcontained);
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void removeState(@Nonnull NonHostEntity entity) throws DatabaseException {
      for (final @Nonnull SiteStorage substorage : substorages.values()) { substorage.removeState(entity); }
    }
    
}
