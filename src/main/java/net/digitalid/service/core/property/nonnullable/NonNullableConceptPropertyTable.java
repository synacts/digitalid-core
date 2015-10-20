package net.digitalid.service.core.property.nonnullable;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.agent.Agent;
import net.digitalid.service.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.agent.Restrictions;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.data.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.factories.ConceptFactories;
import net.digitalid.service.core.factories.Factories;
import net.digitalid.service.core.host.Host;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.property.ConceptPropertyInternalAction;
import net.digitalid.service.core.property.ConceptPropertyTable;
import net.digitalid.service.core.property.StateSelector;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.ListWrapper;
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
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public class NonNullableConceptPropertyTable<V, C extends Concept<C, E, ?>, E extends Entity<E>> extends ConceptPropertyTable<V, C, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @OnMainThread
    protected static @Nonnull @Loaded SemanticType mapDumpType(@Nonnull StateModule module, @Nonnull @Validated String name, @Nonnull ConceptFactories<?, ?> conceptFactories, @Nonnull Factories<?, ?> valueFactories) {
        final @Nonnull String identifier = name + module.getDumpType().getAddress().getStringWithDot();
        final @Nonnull SemanticType entry = SemanticType.map("entry." + identifier).load(TupleWrapper.TYPE, Identity.IDENTIFIER, conceptFactories.getEncodingFactory().getType(), Time.TYPE, valueFactories.getEncodingFactory().getType());
        return SemanticType.map(identifier).load(ListWrapper.TYPE, entry);
    }
    
    @OnMainThread
    protected static @Nonnull @Loaded SemanticType mapStateType(@Nonnull StateModule module, @Nonnull @Validated String name, @Nonnull ConceptFactories<?, ?> conceptFactories, @Nonnull Factories<?, ?> valueFactories) {
        final @Nonnull String identifier = name + module.getStateType().getAddress().getStringWithDot();
        final @Nonnull SemanticType entry = SemanticType.map("entry." + identifier).load(TupleWrapper.TYPE, conceptFactories.getEncodingFactory().getType(), Time.TYPE, valueFactories.getEncodingFactory().getType());
        return SemanticType.map(identifier).load(ListWrapper.TYPE, entry);
    }
    
    private final @Nonnull String identifier = getValueFactories().getEncodingFactory().getType().getAddress().getStringWithDot();
    
    private final @Nonnull @Loaded SemanticType oldValueType = SemanticType.map("old" + identifier).load(getValueFactories().getEncodingFactory().getType());
    
    @Pure
    public final @Nonnull @Loaded SemanticType getOldValueType() {
        return oldValueType;
    }
    
    private final @Nonnull @Loaded SemanticType newValueType = SemanticType.map("new" + identifier).load(getValueFactories().getEncodingFactory().getType());
    
    @Pure
    public final @Nonnull @Loaded SemanticType getNewValueType() {
        return newValueType;
    }
    
    private final @Nonnull @Loaded SemanticType actionType = SemanticType.map("action" + identifier).load(TupleWrapper.TYPE, getConceptFactories().getEncodingFactory().getType(), ConceptPropertyInternalAction.OLD_TIME, ConceptPropertyInternalAction.NEW_TIME, oldValueType, newValueType);
    
    @Pure
    public final @Nonnull @Loaded SemanticType getActionType() {
        return actionType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @OnMainThread
    protected NonNullableConceptPropertyTable(@Nonnull StateModule module, @Nonnull @Validated String name, @Nonnull Factories<E, Site> entityFactories, @Nonnull ConceptFactories<C, E> conceptFactories, @Nonnull Factories<V, ? super E> valueFactories, @Nonnull StateSelector stateSelector) {
        super(module, name, mapDumpType(module, name, conceptFactories, valueFactories), mapStateType(module, name, conceptFactories, valueFactories), entityFactories, conceptFactories, valueFactories, stateSelector);
        
        // TODO: Create and register the method factory of the corresponding action.
    }
    
    @Pure
    @OnMainThread
    public static @Nonnull <V, C extends Concept<C, E, ?>, E extends Entity<E>> NonNullableConceptPropertyTable<V, C, E> get(@Nonnull StateModule module, @Nonnull @Validated String name, @Nonnull Factories<E, Site> entityFactories, @Nonnull ConceptFactories<C, E> conceptFactories, @Nonnull Factories<V, ? super E> valueFactories, @Nonnull StateSelector stateSelector) {
        return new NonNullableConceptPropertyTable<>(module, name, entityFactories, conceptFactories, valueFactories, stateSelector);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– ClientTable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Locked
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + getName(site) + " (" + getEntityFactories().getStoringFactory().getDeclaration() + ", " + getConceptFactories().getStoringFactory().getDeclaration() + ", " + Time.STORING_FACTORY.getDeclaration() + ", " + getValueFactories().getStoringFactory().getDeclaration() + ", PRIMARY KEY (" + getEntityFactories().getStoringFactory().getSelection() + ", " + getConceptFactories().getStoringFactory().getSelection() + ")" + getEntityFactories().getStoringFactory().getForeignKeys(site) + getConceptFactories().getStoringFactory().getForeignKeys(site) + getValueFactories().getStoringFactory().getForeignKeys(site) + ")");
            Database.onInsertIgnore(statement, getName(site), getEntityFactories().getStoringFactory().getSelection(), getConceptFactories().getStoringFactory().getSelection()); // TODO: There is a problem when the entity or the concept uses more than one column because the onInsertIgnore-method expects the arguments differently.
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotIgnore(statement, getName(site));
            statement.executeUpdate("DROP TABLE IF EXISTS " + getName(site));
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– HostTable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull Block exportAll(@Nonnull Host host) throws SQLException {
        final @Nonnull String SQL = "SELECT " + getEntityFactories().getStoringFactory().getSelection() + ", " + getConceptFactories().getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getValueFactories().getStoringFactory().getSelection() + " FROM " + getName(host);
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = FreezableLinkedList.get();
            while (resultSet.next()) {
                int startIndex = 0;
                final @Nonnull E entity = getEntityFactories().getStoringFactory().restoreNonNullable(host, resultSet, startIndex);
                startIndex += getEntityFactories().getStoringFactory().getNumberOfColumns();
                final @Nonnull C concept = getConceptFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                startIndex += getConceptFactories().getStoringFactory().getNumberOfColumns();
                final @Nonnull Time time = Time.STORING_FACTORY.restoreNonNullable(None.OBJECT, resultSet, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                final @Nonnull V value = getValueFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                entries.add(TupleWrapper.encode(getDumpType().getParameters().getNonNullable(0), entity, concept, time, getValueFactories().getEncodingFactory().encodeNonNullable(value)));
            }
            return ListWrapper.encode(getDumpType(), entries.freeze());
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public void importAll(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getDumpType()) : "The block is based on the dump type of this data collection.";
        
        final @Nonnull String SQL = "INSERT INTO " + getName(host) + " (" + getEntityFactories().getStoringFactory().getSelection() + ", " + getConceptFactories().getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getValueFactories().getStoringFactory().getSelection() + ") VALUES (" + getEntityFactories().getStoringFactory().getInsertForPreparedStatement() + ", " + getConceptFactories().getStoringFactory().getInsertForPreparedStatement() + ", " + Time.STORING_FACTORY.getInsertForPreparedStatement() + ", " + getValueFactories().getStoringFactory().getInsertForPreparedStatement() + ")";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            final @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
            for (final @Nonnull Block entry : entries) {
                final @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(4);
                int startIndex = 0;
                
                final @Nonnull E entity = getEntityFactories().getEncodingFactory().decodeNonNullable(host, elements.getNonNullable(0));
                getEntityFactories().getStoringFactory().storeNonNullable(entity, preparedStatement, startIndex);
                startIndex += getEntityFactories().getStoringFactory().getNumberOfColumns();
                
                final @Nonnull C concept = getConceptFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(1));
                getConceptFactories().getStoringFactory().storeNonNullable(concept, preparedStatement, startIndex);
                startIndex += getConceptFactories().getStoringFactory().getNumberOfColumns();
                
                final @Nonnull Time time = Time.ENCODING_FACTORY.decodeNonNullable(None.OBJECT, elements.getNonNullable(2));
                Time.STORING_FACTORY.storeNonNullable(time, preparedStatement, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                
                final @Nonnull V value = getValueFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(3));
                getValueFactories().getStoringFactory().storeNonNullable(value, preparedStatement, startIndex);
                
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– StateTable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull E entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        // TODO: String SQL = select(getConceptFactories(),Time.FACTORIES, getValueFactories()).from(entity).where(factory, object).and().and().toSQL();
        // TODO: Instead of getConceptFactories().getStoringFactory().storeNonNullable and getConceptFactories().getStoringFactory().restoreNonNullable, one could define a store and restore method that also takes a GeneralFactories as a parameter.
        final @Nonnull String SQL = "SELECT " + getConceptFactories().getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getValueFactories().getStoringFactory().getSelection() + " FROM " + getName(entity.getSite()) + " WHERE " + getEntityFactories().getStoringFactory().getConditionForStatement(entity) + " AND " + getStateSelector().getCondition(permissions, restrictions, agent);
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = FreezableLinkedList.get();
            while (resultSet.next()) {
                int startIndex = 0;
                final @Nonnull C concept = getConceptFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                startIndex += getConceptFactories().getStoringFactory().getNumberOfColumns();
                final @Nonnull Time time = Time.STORING_FACTORY.restoreNonNullable(None.OBJECT, resultSet, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                final @Nonnull V value = getValueFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                entries.add(TupleWrapper.encode(getStateType().getParameters().getNonNullable(0), concept, time, getValueFactories().getEncodingFactory().encodeNonNullable(value)));
            }
            return ListWrapper.encode(getStateType(), entries.freeze());
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public void addState(@Nonnull E entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getStateType()) : "The block is based on the state type of this data collection.";
        
        final @Nonnull String SQL = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + getName(entity.getSite()) + " (" + getEntityFactories().getStoringFactory().getSelection() + ", " + getConceptFactories().getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getValueFactories().getStoringFactory().getSelection() + ") VALUES (" + getEntityFactories().getStoringFactory().getInsertForPreparedStatement() + ", " + getConceptFactories().getStoringFactory().getInsertForPreparedStatement() + ", " + Time.STORING_FACTORY.getInsertForPreparedStatement() + ", " + getValueFactories().getStoringFactory().getInsertForPreparedStatement() + ")";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            final @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
            for (final @Nonnull Block entry : entries) {
                final @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                int startIndex = 0;
                
                getEntityFactories().getStoringFactory().storeNonNullable(entity, preparedStatement, startIndex);
                startIndex += getEntityFactories().getStoringFactory().getNumberOfColumns();
                
                final @Nonnull C concept = getConceptFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(0));
                getConceptFactories().getStoringFactory().storeNonNullable(concept, preparedStatement, startIndex);
                startIndex += getConceptFactories().getStoringFactory().getNumberOfColumns();
                
                final @Nonnull Time time = Time.ENCODING_FACTORY.decodeNonNullable(None.OBJECT, elements.getNonNullable(1));
                Time.STORING_FACTORY.storeNonNullable(time, preparedStatement, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                
                final @Nonnull V value = getValueFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(2));
                getValueFactories().getStoringFactory().storeNonNullable(value, preparedStatement, startIndex);
                
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        getConceptFactories().getStoringFactory().getIndex().reset(entity, this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Capturable @Nonnull @NonNullableElements @Frozen ReadOnlyPair<Time, V> load(@Nonnull NonNullableConceptProperty<V, C, E> property) throws SQLException {
        final @Nonnull E entity = property.getConcept().getEntity();
        final @Nonnull String SQL = "SELECT " + Time.STORING_FACTORY.getSelection() + ", " + getValueFactories().getStoringFactory().getSelection() + " FROM " + getName(entity.getSite()) + " WHERE " + getEntityFactories().getStoringFactory().getConditionForStatement(entity) + " AND " + getConceptFactories().getStoringFactory().getConditionForStatement(property.getConcept());
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                int startIndex = 0;
                final @Nonnull Time time = Time.STORING_FACTORY.restoreNonNullable(None.OBJECT, resultSet, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                final @Nonnull V value = getValueFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                if (!property.getValidator().isValid(value)) throw new SQLException("The value of the given property is invalid.");
                return FreezablePair.get(time, value).freeze();
            } else throw new SQLException("No value found for the given property.");
        }
    }
    
    void replace(@Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull @Validated V oldValue, @Nonnull @Validated V newValue) throws SQLException {
        final @Nonnull E entity = property.getConcept().getEntity();
        final @Nonnull String SQL = "UPDATE " + getName(entity.getSite()) + " SET " + Time.STORING_FACTORY.getUpdateForStatement(newTime) + ", " + getValueFactories().getStoringFactory().getUpdateForPreparedStatement() + " WHERE " + getEntityFactories().getStoringFactory().getConditionForStatement(entity) + " AND " + getConceptFactories().getStoringFactory().getConditionForStatement(property.getConcept()) + " AND " + Time.STORING_FACTORY.getConditionForStatement(oldTime) + " AND " + getValueFactories().getStoringFactory().getConditionForPreparedStatement();
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            int startIndex = 0;
            getValueFactories().getStoringFactory().storeNonNullable(newValue, preparedStatement, startIndex);
            startIndex += getValueFactories().getStoringFactory().getNumberOfColumns();
            getValueFactories().getStoringFactory().storeNonNullable(oldValue, preparedStatement, startIndex);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The value of the given property could not be replaced.");
        }
    }
    
}
