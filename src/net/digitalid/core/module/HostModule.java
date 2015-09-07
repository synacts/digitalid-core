package net.digitalid.core.module;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.OnMainThread;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.service.Service;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.SelfcontainedWrapper;

/**
 * Host modules are only used on the {@link Host host}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class HostModule<T extends HostTable<T>> extends Module<T> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code table.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TABLE = SemanticType.map("table.module@core.digitalid.net").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.map("module@core.digitalid.net").load(ListWrapper.TYPE, TABLE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the type of this module.
     */
    private final @Nonnull @Loaded SemanticType moduleType;
    
    /**
     * Returns the type of this module.
     * 
     * @return the type of this module.
     */
    @Pure
    public final @Nonnull @Loaded SemanticType getModuleType() {
        return moduleType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new host module with the given service and identifier.
     * 
     * @param service the service to which the new module belongs.
     * @param identifier the common identifier of the new module.
     * 
     * @require InternalNonHostIdentifier.isValid(identifier) : "The string is a valid internal non-host identifier.";
     */
    @OnMainThread
    protected HostModule(@Nonnull Service service, @Nonnull String identifier) {
        super(service);
        
        this.moduleType = SemanticType.map("module." + identifier).load(MODULE);
    }
    
    /**
     * Returns a new host module with the given service and identifier.
     * 
     * @param service the service to which the new module belongs.
     * @param identifier the common identifier of the new module.
     * 
     * @return a new host module with the given service and identifier.
     * 
     * @require InternalNonHostIdentifier.isValid(identifier) : "The string is a valid internal non-host identifier.";
     */
    @Pure
    public static @Nonnull <T extends HostTable<T>> HostModule<T> get(@Nonnull Service service, @Nonnull String identifier) {
        return new HostModule<>(service, identifier);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Export and Import –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Exports this module encoded as a block.
     * 
     * @param host the host which is exported.
     * 
     * @return this module encoded as a block.
     * 
     * @ensure return.getType().equals(getModuleType()) : "The returned block has the type of this module.";
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nonnull Block exportAll(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> tables = FreezableLinkedList.get();
        for (final @Nonnull T table : getTables()) tables.add(SelfcontainedWrapper.encodeNonNullable(TABLE, table.exportAll(host)));
        return ListWrapper.encode(moduleType, tables.freeze());
    }
    
    /**
     * Returns the table with the given type.
     * 
     * @param tableType the type of the table.
     * 
     * @return the table with the given type.
     */
    @Pure
    public final @Nonnull T getTable(@Nonnull SemanticType tableType) throws InvalidEncodingException {
        for (final @Nonnull T table : getTables()) {
            if (table.getTableType().equals(tableType)) return table;
        }
        throw new InvalidEncodingException("The module " + moduleType + " does not include the table " + tableType + ".");
    }
    
    /**
     * Imports this module for the given host from the given block.
     * 
     * @param host the host for whom this module is to be imported.
     * @param block the block containing the data of this module.
     * 
     * @require block.getType().isBasedOn(getModuleType()) : "The block is based on the type of this module.";
     */
    @Locked
    @NonCommitting
    public void importAll(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull @NonNullableElements ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block element : elements) {
            final @Nonnull Block table = SelfcontainedWrapper.decodeNonNullable(element);
            getTable(table.getType()).importAll(host, table);
        }
    }
    
}
