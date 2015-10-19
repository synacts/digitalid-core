package net.digitalid.service.core.property.nonnullable;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.agent.Agent;
import net.digitalid.service.core.agent.ClientAgent;
import net.digitalid.service.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.agent.Restrictions;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.client.Client;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.data.StateModule;
import net.digitalid.service.core.entity.Account;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.EntityClass;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.storable.StorableFactory;
import net.digitalid.service.core.host.Host;
import net.digitalid.service.core.identifier.IdentifierClass;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.password.Password;
import net.digitalid.service.core.property.ConceptPropertyInternalAction;
import net.digitalid.service.core.property.ConceptPropertyTable;
import net.digitalid.service.core.property.StateSelector;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.ListWrapper;
import net.digitalid.service.core.wrappers.StringWrapper;
import net.digitalid.service.core.wrappers.TupleWrapper;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.collections.tuples.FreezablePair;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.annotations.OnMainThread;
import net.digitalid.utility.database.column.Site;
import net.digitalid.utility.database.configuration.Database;

/**
 * Description.
 * 
 * Each table has a name and some columns:
 * – the entity
 * – the time
 * – a storable object?
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
@Immutable
public class NonNullableConceptPropertyTable<V, C extends Concept<C, E, ?>, E extends Entity> extends ConceptPropertyTable<V, C, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @OnMainThread
    protected static @Nonnull @Loaded SemanticType mapDumpType(@Nonnull StateModule module, @Nonnull @Validated String name, @Nonnull Concept.IndexBasedGlobalFactory<?, ?, ?> conceptFactory, @Nonnull StorableFactory<?, ?> valueFactory) {
        final @Nonnull String identifier = name + module.getDumpType().getAddress().getStringWithDot();
        final @Nonnull SemanticType entry = SemanticType.map("entry." + identifier).load(TupleWrapper.TYPE, Identity.IDENTIFIER, conceptFactory.getType(), Time.TYPE, valueFactory.getType());
        return SemanticType.map(identifier).load(ListWrapper.TYPE, entry);
    }
    
    @OnMainThread
    protected static @Nonnull @Loaded SemanticType mapStateType(@Nonnull StateModule module, @Nonnull @Validated String name, @Nonnull Concept.IndexBasedGlobalFactory<?, ?, ?> conceptFactory, @Nonnull StorableFactory<?, ?> valueFactory) {
        final @Nonnull String identifier = name + module.getStateType().getAddress().getStringWithDot();
        final @Nonnull SemanticType entry = SemanticType.map("entry." + identifier).load(TupleWrapper.TYPE, conceptFactory.getType(), Time.TYPE, valueFactory.getType());
        return SemanticType.map(identifier).load(ListWrapper.TYPE, entry);
    }
    
    private final @Nonnull String identifier = getValueFactory().getType().getAddress().getStringWithDot();
    
    private final @Nonnull @Loaded SemanticType oldValueType = SemanticType.map("old" + identifier).load(getValueFactory().getType());
    
    @Pure
    public final @Nonnull @Loaded SemanticType getOldValueType() {
        return oldValueType;
    }
    
    private final @Nonnull @Loaded SemanticType newValueType = SemanticType.map("new" + identifier).load(getValueFactory().getType());
    
    @Pure
    public final @Nonnull @Loaded SemanticType getNewValueType() {
        return newValueType;
    }
    
    private final @Nonnull @Loaded SemanticType actionType = SemanticType.map("action" + identifier).load(TupleWrapper.TYPE, getConceptFactory().getType(), ConceptPropertyInternalAction.OLD_TIME, ConceptPropertyInternalAction.NEW_TIME, oldValueType, newValueType);
    
    @Pure
    public final @Nonnull @Loaded SemanticType getActionType() {
        return actionType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @OnMainThread
    protected NonNullableConceptPropertyTable(@Nonnull StateModule module, @Nonnull @Validated String name, @Nonnull StorableFactory<E, Site> entityFactory, @Nonnull Concept.IndexBasedGlobalFactory<C, E, ?> conceptFactory, @Nonnull StorableFactory<V, ? super E> valueFactory, @Nonnull StateSelector stateSelector) {
        super(module, name, mapDumpType(module, name, conceptFactory, valueFactory), mapStateType(module, name, conceptFactory, valueFactory), entityFactory, conceptFactory, valueFactory, stateSelector);
    }
    
    @Pure
    @OnMainThread
    public static @Nonnull <V, C extends Concept<C, E, ?>, E extends Entity> NonNullableConceptPropertyTable<V, C, E> get(@Nonnull StateModule module, @Nonnull @Validated String name, @Nonnull StorableFactory<E, Site> entityFactory, @Nonnull Concept.IndexBasedGlobalFactory<C, E, ?> conceptFactory, @Nonnull StorableFactory<V, ? super E> valueFactory, @Nonnull StateSelector stateSelector) {
        return new NonNullableConceptPropertyTable<>(module, name, entityFactory, conceptFactory, valueFactory, stateSelector);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– ClientTable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Locked
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Adapt the copy-paste!
            Password.FACTORY.getColumns();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + getName() + " (entity " + EntityClass.FORMAT + " NOT NULL, password VARCHAR(50) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", PRIMARY KEY (entity), FOREIGN KEY (entity) " + site.getEntityReference() + ")");
            Database.onInsertIgnore(statement, site + getName(), "entity", "concept"); // TODO: Replace "concept" with column name.
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotIgnore(statement, site + getName());
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + getName());
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– HostTable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code entry.password.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.password.module@core.digitalid.net").load(TupleWrapper.TYPE, Identity.IDENTIFIER, Password.TYPE);
    
    /**
     * Stores the semantic type {@code password.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("password.module@core.digitalid.net").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull Block exportAll(@Nonnull Host host) throws SQLException {
        // TODO: Adapt!
        final @Nonnull String SQL = "SELECT entity, password FROM " + host + "password";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
            while (resultSet.next()) {
                final @Nonnull Account account = Account.getNotNull(host, resultSet, 1);
                final @Nonnull String password = resultSet.getString(2);
                entries.add(new TupleWrapper(MODULE_ENTRY, account.getIdentity().getAddress(), new StringWrapper(Password.TYPE, password)).toBlock());
            }
            return new ListWrapper(MODULE_FORMAT, entries.freeze()).toBlock();
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public void importAll(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getDumpType()) : "The block is based on the format of this module.";
        
        // TODO: Adapt!
        
        // @Nonnull Entity entity = Account.get(host, IdentityClass.create(block).toInternalIdentity());
        
        final @Nonnull E entity = getEntityFactory().decodeNonNullable(host, block);
        
        getConceptFactory().decodeNonNullable(entity, block);
        
        final @Nonnull String SQL = "INSERT INTO " + host + "password (entity, password) VALUES (?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(entry).getNonNullableElements(2);
                preparedStatement.setLong(1, IdentifierClass.create(elements.getNonNullable(0)).getIdentity().toInternalNonHostIdentity().getNumber());
                preparedStatement.setString(2, new StringWrapper(elements.getNonNullable(1)).getString());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– StateTable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code passwords.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.map("passwords.state@core.digitalid.net").load(TupleWrapper.TYPE, Password.TYPE);
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull E entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        // Difficulty: How to select the state given the authorization?
        return new TupleWrapper(STATE_FORMAT, restrictions.isClient() ? new StringWrapper(Password.TYPE, get(entity)) : null).toBlock();
    }
    
    @Locked
    @Override
    @NonCommitting
    public void addState(@Nonnull E entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getStateType()) : "The block is based on the indicated type.";
        
        // TODO: Adapt!
        
//        final @Nullable Block element = new TupleWrapper(block).getNullableElement(0);
//        if (element != null) set(entity, new StringWrapper(element).getString());
        
        getConceptFactory().getIndex().reset(entity, this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Capturable @Nonnull @NonNullableElements @Frozen ReadOnlyPair<Time, V> load(@Nonnull NonNullableConceptProperty<V, C, E> property) throws SQLException {
        V v = null;
        return FreezablePair.get(Time.getCurrent(), v).freeze();
    }
    
    void replace(@Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull V oldValue, @Nonnull V newValue) throws SQLException {
        // TODO!
    }
    
    
    // TODO: The following code serves just as an example and should be removed afterwards.
    
    /**
     * Returns the name of the given client agent.
     * 
     * @param clientAgent the client agent whose name is to be returned.
     * 
     * @return the name of the given client agent.
     * 
     * @ensure Client.isValid(return) : "The returned name is valid.";
     */
    @Pure
    @NonCommitting
    static @Nonnull String getName(@Nonnull ClientAgent clientAgent) throws SQLException {
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "SELECT name FROM " + entity.getSite() + "client_agent WHERE entity = " + entity + " AND agent = " + clientAgent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull String name = resultSet.getString(1);
                if (!Client.isValidName(name)) throw new SQLException("The name of the client agent with the number " + clientAgent + " is invalid.");
                return name;
            } else throw new SQLException("The given client agent has no name.");
        }
    }
    
    /**
     * Replaces the name of the given client agent.
     * 
     * @param clientAgent the client agent whose name is to be replaced.
     * @param oldName the old name of the given client agent.
     * @param newName the new name of the given client agent.
     * 
     * @require Client.isValid(oldName) : "The old name is valid.";
     * @require Client.isValid(newName) : "The new name is valid.";
     */
    @NonCommitting
    static void replaceName(@Nonnull ClientAgent clientAgent, @Nonnull String oldName, @Nonnull String newName) throws SQLException {
        assert Client.isValidName(oldName) : "The old name is valid.";
        assert Client.isValidName(newName) : "The new name is valid.";
        
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "UPDATE " + entity.getSite() + "client_agent SET name = ? WHERE entity = " + entity + " AND agent = " + clientAgent + " AND name = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            preparedStatement.setString(1, newName);
            preparedStatement.setString(2, oldName);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The name of the client agent with the number " + clientAgent + " could not be replaced.");
        }
    }
    
}
