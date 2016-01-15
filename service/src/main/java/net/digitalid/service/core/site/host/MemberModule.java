package net.digitalid.service.core.site.host;

import java.sql.Statement;

import javax.annotation.Nonnull;

import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.validation.state.Stateless;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.table.Site;

import net.digitalid.service.core.CoreService;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.service.core.block.wrappers.structure.TupleWrapper;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.storage.HostModule;
import net.digitalid.service.core.storage.Service;

/**
 * This class provides database access to the members of the core service.
 * The members of a host can create as many new identities as they want.
 */
@Stateless
public final class MemberModule implements HostModule {
    
    /**
     * Stores an instance of this module.
     */
    public static final MemberModule MODULE = new MemberModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Create the tables of this module.
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.members.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.members.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, net.digitalid.service.core.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code members.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("members.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws DatabaseException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve all the entries from the database table(s).
        }
        return ListWrapper.encode(MODULE_FORMAT, entries.freeze());
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws DatabaseException, InvalidEncodingException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block entry : entries) {
            // TODO: Add all entries to the database table(s).
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
