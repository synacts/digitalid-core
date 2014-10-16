package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.handler.InternalQuery;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.CoreService;
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
import javax.annotation.Nullable;

/**
 * This class provides database access to the certificates of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Certificates implements BothModule {
    
    static { CoreService.SERVICE.add(new Certificates()); }
    
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
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.create("entry.pushing.module@virtualid.ch").load(TupleWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code pushing.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.create("pushing.module@virtualid.ch").load(ListWrapper.TYPE, MODULE_ENTRY);
    
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
    
    
    /**
     * Stores the semantic type {@code entry.pushing.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE_ENTRY = SemanticType.create("entry.pushing.state@virtualid.ch").load(TupleWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code pushing.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.create("pushing.state@virtualid.ch").load(ListWrapper.TYPE, STATE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE;
    }
    
    @Pure
    @Override
    public @Nonnull Block getState(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
            // TODO: Retrieve the entries of the given entity from the database table(s).
        }
        return new ListWrapper(STATE, entries.freeze()).toBlock();
    }
    
    @Override
    public void addState(@Nonnull Entity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add the entries of the given entity to the database table(s).
        }
    }
    
    @Override
    public void removeState(@Nonnull Entity entity) throws SQLException {
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
            // TODO: Remove the entries of the given entity from the database table(s).
        }
    }
    
    @Pure
    @Override
    public @Nullable InternalQuery getInternalQuery(@Nonnull Role role) {
        return null; // TODO: Return the internal query for reloading the data of this module.
    }
    
    
    /**
     * Stores the semantic type {@code certificates.module@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("certificates.module@virtualid.ch").load(TupleWrapper.TYPE, );
    
    @Pure
    @Override
    public @Nonnull SemanticType getFormat() {
        return TYPE;
    }
    
    
    static { CoreService.SERVICE.add(new Certificates()); }
    
    @Override
    protected void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS certificate (issuer BIGINT NOT NULL, recipient BIGINT NOT NULL, type BIGINT NOT NULL, value LONGBLOB NOT NULL, issuance BIGINT, PRIMARY KEY (issuer, recipient, type), FOREIGN KEY (issuer) REFERENCES map_identity (identity), FOREIGN KEY (recipient) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
        }
        
        Mapper.addReference("certificate", "recipient");
    }
    
    
    static void getCertificate() {
        // TODO
    }
    
    
    /**
     * Returns the state of the given entity restricted by the authorization of the given agent.
     * 
     * @param entity the entity whose state is to be returned.
     * @param agent the agent whose authorization restricts the returned state.
     * @return the state of the given entity restricted by the authorization of the given agent.
     */
    @Override
    protected @Nonnull Block getAll(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        return Block.EMPTY;
    }
    
    /**
     * Adds the state in the given block to the given entity.
     * 
     * @param entity the entity to which the state is to be added.
     * @param block the block containing the state to be added.
     */
    @Override
    protected void addAll(@Nonnull Entity entity, @Nonnull Block block) throws SQLException {
        
    }
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    @Override
    protected void removeAll(@Nonnull Entity entity) throws SQLException {
        
    }
    
}
