package ch.virtualid.module.host;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.virtualid.module.HostModule;
import ch.virtualid.server.Host;
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
 * This class provides database access to the credentials of the core service.
 * 
 * A log for issued credentials is needed on the host-side.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class HostCredentials implements HostModule {
    
    static { CoreService.SERVICE.add(new HostCredentials()); }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
            // TODO: Create the tables of this module.
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.pushing.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ENTRY = SemanticType.create("entry.pushing.module@virtualid.ch").load(TupleWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code pushing.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.create("pushing.module@virtualid.ch").load(ListWrapper.TYPE, ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE;
    }
    
    @Pure
    @Override
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
            // TODO: Retrieve all the entries from the database table(s).
        }
        return new ListWrapper(MODULE, entries.freeze()).toBlock();
    }
    
    @Override
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadonlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add all entries to the database table(s).
        }
    }
    
    
    static { CoreService.SERVICE.add(new HostCredentials()); }
    
    @Override
    protected void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            
        }
    }
    
}
