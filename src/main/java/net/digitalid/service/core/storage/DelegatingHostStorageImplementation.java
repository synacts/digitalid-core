package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.SelfcontainedWrapper;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestErrorCode;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
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
 * This class implements a storage that delegates the export and import to substorages on {@link Host hosts}.
 * 
 * @see HostModule
 * @see DelegatingSiteStorageImplementation
 */
@Immutable
abstract class DelegatingHostStorageImplementation extends DelegatingClientStorageImplementation implements HostStorage {
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code table.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TABLE = SemanticType.map("table.module@core.digitalid.net").load(SelfcontainedWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.map("module@core.digitalid.net").load(ListWrapper.XDF_TYPE, TABLE);
    
    /* -------------------------------------------------- Dump Type -------------------------------------------------- */
    
    /**
     * Stores the dump type of this host storage.
     */
    private final @Nonnull @Loaded SemanticType dumpType;
    
    @Pure
    @Override
    public final @Nonnull @Loaded SemanticType getDumpType() {
        return dumpType;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new host storage with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     */
    @OnMainThread
    protected DelegatingHostStorageImplementation(@Nullable Service service, @Nonnull @Validated String name) {
        super(service, name);
        
        final @Nonnull String identifier;
        if (service == null) {
            identifier = "module.service" + ((Service) this).getType().getAddress().getStringWithDot();
        } else {
            identifier = "module." + name + service.getType().getAddress().getStringWithDot();
        }
        this.dumpType = SemanticType.map(identifier).load(MODULE);
    }
    
    /* -------------------------------------------------- Substorages -------------------------------------------------- */
    
    /**
     * Stores the substorages of this storage.
     */
    private final @Nonnull @NonNullableElements @NonFrozen FreezableMap<SemanticType, HostStorage> substorages = FreezableLinkedHashMap.get();
    
    /**
     * Registers the given substorage at this storage.
     * 
     * @param substorage the substorage to be registered.
     */
    final void registerHostStorage(@Nonnull HostStorage substorage) {
        substorages.put(substorage.getDumpType(), substorage);
        super.registerClientStorage(substorage);
    }
    
    /* -------------------------------------------------- Export and Import -------------------------------------------------- */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public final @Nonnull Block exportAll(@Nonnull Host host) throws DatabaseException {
        final @Nonnull FreezableList<Block> elements = FreezableArrayList.getWithCapacity(substorages.size());
        for (final @Nonnull HostStorage substorage : substorages.values()) { elements.add(SelfcontainedWrapper.encodeNonNullable(TABLE, substorage.exportAll(host))); }
        return ListWrapper.encode(dumpType, elements.freeze());
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void importAll(@Nonnull Host host, @Nonnull Block block) throws DatabaseException, RequestException, ExternalException, NetworkException {
        final @Nonnull @NonNullableElements ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block selfcontained = SelfcontainedWrapper.decodeNonNullable(element);
            final @Nullable HostStorage substorage = substorages.get(selfcontained.getType());
            if (substorage == null) { throw new RequestException(RequestErrorCode.CONTENT, "There is no substorage for the block of type " + selfcontained.getType() + "."); }
            substorage.importAll(host, selfcontained);
        }
    }
    
}
