package net.digitalid.core.state;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashMap;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.freezable.FreezableMap;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.system.thread.annotations.MainThread;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.client.Client;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.SelfcontainedWrapper;
import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestErrorCode;
import net.digitalid.core.exceptions.RequestException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.annotations.Loaded;

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
    @MainThread
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
    public final void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        final @Nonnull @NonNullableElements ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block selfcontained = SelfcontainedWrapper.decodeNonNullable(element);
            final @Nullable SiteStorage substorage = substorages.get(selfcontained.getType());
            if (substorage == null) { throw RequestException.get(RequestErrorCode.CONTENT, "There is no table for the block of type " + selfcontained.getType() + "."); }
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
