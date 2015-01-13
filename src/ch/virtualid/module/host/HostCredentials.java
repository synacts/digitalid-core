package ch.virtualid.module.host;

import ch.virtualid.annotations.Pure;
import ch.virtualid.credential.Credential;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.HostModule;
import ch.virtualid.server.Host;
import ch.virtualid.service.CoreService;
import ch.virtualid.service.Service;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

/**
 * This class provides database access to the {@link Credential credentials} issued by a {@link Host host}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class HostCredentials implements HostModule {
    
    public static final HostCredentials MODULE = new HostCredentials();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Create the tables of this module.
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.host.credentials.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.create("entry.host.credentials.module@virtualid.ch").load(TupleWrapper.TYPE, ch.virtualid.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code host.credentials.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.create("host.credentials.module@virtualid.ch").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve all the entries from the database table(s).
        }
        return new ListWrapper(MODULE_FORMAT, entries.freeze()).toBlock();
    }
    
    @Override
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadonlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add all entries to the database table(s).
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
