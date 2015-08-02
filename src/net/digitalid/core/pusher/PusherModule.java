package net.digitalid.core.pusher;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Stateless;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.handler.ExternalAction;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class provides database access to the {@link ExternalAction external actions} that still need to be {@link Pusher pushed}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
@Stateless
public final class PusherModule implements BothModule {
    
    /**
     * Stores an instance of this module.
     */
    public static final PusherModule MODULE = new PusherModule();
    
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
     * Stores the semantic type {@code entry.pusher.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.pusher.module@core.digitalid.net").load(TupleWrapper.TYPE, net.digitalid.core.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code pusher.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("pusher.module@core.digitalid.net").load(ListWrapper.TYPE, MODULE_ENTRY);
    
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
        
        final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add all entries to the database table(s).
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.pusher.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_ENTRY = SemanticType.map("entry.pusher.state@core.digitalid.net").load(TupleWrapper.TYPE, net.digitalid.core.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code pusher.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.map("pusher.state@core.digitalid.net").load(ListWrapper.TYPE, STATE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve the entries of the given entity from the database table(s).
        }
        return new ListWrapper(STATE_FORMAT, entries.freeze()).toBlock();
    }
    
    @Override
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add the entries of the given entity to the database table(s).
        }
    }
    
    @Override
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Remove the entries of the given entity from the database table(s).
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
