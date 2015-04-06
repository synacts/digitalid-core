package net.digitalid.core.host;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadonlyList;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.module.HostModule;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class provides database access to the members of the core service.
 * The members of a host can create as many new identities as they want.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
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
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Create the tables of this module.
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.members.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.create("entry.members.module@core.digitalid.net").load(TupleWrapper.TYPE, net.digitalid.core.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code members.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.create("members.module@core.digitalid.net").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve all the entries from the database table(s).
        }
        return new ListWrapper(MODULE_FORMAT, entries.freeze()).toBlock();
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadonlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add all entries to the database table(s).
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
