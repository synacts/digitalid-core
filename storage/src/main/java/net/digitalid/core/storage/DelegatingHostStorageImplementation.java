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

import net.digitalid.database.annotations.transaction.Locked;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.SelfcontainedWrapper;
import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestErrorCode;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.annotations.Loaded;

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
    @MainThread
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
    public final void importAll(@Nonnull Host host, @Nonnull Block block) throws ExternalException {
        final @Nonnull @NonNullableElements ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block selfcontained = SelfcontainedWrapper.decodeNonNullable(element);
            final @Nullable HostStorage substorage = substorages.get(selfcontained.getType());
            if (substorage == null) { throw RequestException.get(RequestErrorCode.CONTENT, "There is no substorage for the block of type " + selfcontained.getType() + "."); }
            substorage.importAll(host, selfcontained);
        }
    }
    
}
