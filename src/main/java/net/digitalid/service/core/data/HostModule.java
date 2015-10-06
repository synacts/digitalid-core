package net.digitalid.service.core.data;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.host.Host;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.ListWrapper;
import net.digitalid.service.core.wrappers.SelfcontainedWrapper;
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

/**
 * Host modules are only used on {@link Host hosts}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public class HostModule extends ClientModule implements HostData {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code table.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TABLE = SemanticType.map("table.module@core.digitalid.net").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.map("module@core.digitalid.net").load(ListWrapper.TYPE, TABLE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Dump Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the dump type of this module.
     */
    private final @Nonnull @Loaded SemanticType dumpType;
    
    @Pure
    @Override
    public final @Nonnull @Loaded SemanticType getDumpType() {
        return dumpType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new host module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     */
    @OnMainThread
    protected HostModule(@Nullable Service service, @Nonnull @Validated String name) {
        super(service, name);
        
        final @Nonnull String identifier;
        if (service == null) {
            identifier = "module.service" + ((Service) this).getType().getAddress().getStringWithDot();
        } else {
            identifier = "module." + name + service.getType().getAddress().getStringWithDot();
            service.register(this);
        }
        this.dumpType = SemanticType.map(identifier).load(MODULE);
    }
    
    /**
     * Returns a new host module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     * 
     * @return a new host module with the given service and name.
     */
    @Pure
    @OnMainThread
    public static @Nonnull HostModule get(@Nonnull Service service, @Nonnull @Validated String name) {
        return new HostModule(service, name);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Sites –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final boolean isForHosts() {
        return true;
    }
    
    @Pure
    @Override
    public boolean isForClients() {
        return false;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Tables –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the tables of this module.
     */
    private final @Nonnull @NonNullableElements @NonFrozen FreezableMap<SemanticType, HostData> tables = FreezableLinkedHashMap.get();
    
    /**
     * Registers the given table at this module.
     * 
     * @param table the table to be registered.
     */
    final void register(@Nonnull HostData table) {
        tables.put(table.getDumpType(), table);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Data –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public final @Nonnull Block exportAll(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> elements = FreezableArrayList.getWithCapacity(tables.size());
        for (final @Nonnull HostData table : tables.values()) elements.add(SelfcontainedWrapper.encodeNonNullable(TABLE, table.exportAll(host)));
        return ListWrapper.encode(dumpType, elements.freeze());
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void importAll(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull @NonNullableElements ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block selfcontained = SelfcontainedWrapper.decodeNonNullable(element);
            final @Nullable HostData table = tables.get(selfcontained.getType());
            if (table == null) throw new InvalidEncodingException("There is no table for the block of type " + selfcontained.getType() + ".");
            table.importAll(host, selfcontained);
        }
    }
    
}
