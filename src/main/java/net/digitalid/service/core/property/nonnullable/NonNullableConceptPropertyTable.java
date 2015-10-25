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
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.host.Host;
import net.digitalid.service.core.property.ConceptPropertyTable;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.ListWrapper;
import net.digitalid.service.core.wrappers.TupleWrapper;
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
 */
@Immutable
public final class NonNullableConceptPropertyTable<V, C extends Concept<C, E, ?>, E extends Entity<E>> extends ConceptPropertyTable<V, C, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @OnMainThread
    NonNullableConceptPropertyTable(@Nonnull NonNullableConceptPropertyFactory<V, C, E> propertyFactory) {
        super(propertyFactory);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– ClientTable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Locked
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + getName(site) + " (" + getPropertyFactory().getEntityFactories().getStoringFactory().getDeclaration() + ", " + getPropertyFactory().getConceptFactories().getStoringFactory().getDeclaration() + ", " + Time.STORING_FACTORY.getDeclaration() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getDeclaration() + ", PRIMARY KEY (" + getPropertyFactory().getEntityFactories().getStoringFactory().getSelection() + ", " + getPropertyFactory().getConceptFactories().getStoringFactory().getSelection() + ")" + getPropertyFactory().getEntityFactories().getStoringFactory().getForeignKeys(site) + getPropertyFactory().getConceptFactories().getStoringFactory().getForeignKeys(site) + getPropertyFactory().getValueFactories().getStoringFactory().getForeignKeys(site) + ")");
            Database.onInsertIgnore(statement, getName(site), getPropertyFactory().getEntityFactories().getStoringFactory().getSelection(), getPropertyFactory().getConceptFactories().getStoringFactory().getSelection()); // TODO: There is a problem when the entity or the concept uses more than one column because the onInsertIgnore-method expects the arguments differently.
            // TODO: Shouldn't we detect here whether we need to call Mapper.addReference?
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
        final @Nonnull String SQL = "SELECT " + getPropertyFactory().getEntityFactories().getStoringFactory().getSelection() + ", " + getPropertyFactory().getConceptFactories().getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getSelection() + " FROM " + getName(host);
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = FreezableLinkedList.get();
            while (resultSet.next()) {
                int startIndex = 0;
                final @Nonnull E entity = getPropertyFactory().getEntityFactories().getStoringFactory().restoreNonNullable(host, resultSet, startIndex);
                startIndex += getPropertyFactory().getEntityFactories().getStoringFactory().getNumberOfColumns();
                final @Nonnull C concept = getPropertyFactory().getConceptFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                startIndex += getPropertyFactory().getConceptFactories().getStoringFactory().getNumberOfColumns();
                final @Nonnull Time time = Time.STORING_FACTORY.restoreNonNullable(None.OBJECT, resultSet, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                final @Nonnull V value = getPropertyFactory().getValueFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                entries.add(TupleWrapper.encode(getDumpType().getParameters().getNonNullable(0), entity, concept, time, getPropertyFactory().getValueFactories().getEncodingFactory().encodeNonNullable(value)));
            }
            return ListWrapper.encode(getDumpType(), entries.freeze());
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public void importAll(@Nonnull Host host, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        assert block.getType().isBasedOn(getDumpType()) : "The block is based on the dump type of this data collection.";
        
        final @Nonnull String SQL = "INSERT INTO " + getName(host) + " (" + getPropertyFactory().getEntityFactories().getStoringFactory().getSelection() + ", " + getPropertyFactory().getConceptFactories().getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getSelection() + ") VALUES (" + getPropertyFactory().getEntityFactories().getStoringFactory().getInsertForPreparedStatement() + ", " + getPropertyFactory().getConceptFactories().getStoringFactory().getInsertForPreparedStatement() + ", " + Time.STORING_FACTORY.getInsertForPreparedStatement() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getInsertForPreparedStatement() + ")";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            final @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
            for (final @Nonnull Block entry : entries) {
                final @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(4);
                int startIndex = 0;
                
                final @Nonnull E entity = getPropertyFactory().getEntityFactories().getEncodingFactory().decodeNonNullable(host, elements.getNonNullable(0));
                getPropertyFactory().getEntityFactories().getStoringFactory().storeNonNullable(entity, preparedStatement, startIndex);
                startIndex += getPropertyFactory().getEntityFactories().getStoringFactory().getNumberOfColumns();
                
                final @Nonnull C concept = getPropertyFactory().getConceptFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(1));
                getPropertyFactory().getConceptFactories().getStoringFactory().storeNonNullable(concept, preparedStatement, startIndex);
                startIndex += getPropertyFactory().getConceptFactories().getStoringFactory().getNumberOfColumns();
                
                final @Nonnull Time time = Time.ENCODING_FACTORY.decodeNonNullable(None.OBJECT, elements.getNonNullable(2));
                Time.STORING_FACTORY.storeNonNullable(time, preparedStatement, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                
                final @Nonnull V value = getPropertyFactory().getValueFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(3));
                getPropertyFactory().getValueFactories().getStoringFactory().storeNonNullable(value, preparedStatement, startIndex);
                
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
        // TODO: String SQL = select(getConceptFactories(),Time.FACTORIES, getPropertyFactory().getValueFactories()).from(entity).where(factory, object).and().and().toSQL();
        // TODO: Instead of getPropertyFactory().getConceptFactories().getStoringFactory().storeNonNullable and getPropertyFactory().getConceptFactories().getStoringFactory().restoreNonNullable, one could define a store and restore method that also takes a GeneralFactories as a parameter.
        final @Nonnull String SQL = "SELECT " + getPropertyFactory().getConceptFactories().getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getSelection() + " FROM " + getName(entity.getSite()) + " WHERE " + getPropertyFactory().getEntityFactories().getStoringFactory().getConditionForStatement(entity) + " AND " + getPropertyFactory().getRequiredAuthorization().getStateFilter(permissions, restrictions, agent);
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = FreezableLinkedList.get();
            while (resultSet.next()) {
                int startIndex = 0;
                final @Nonnull C concept = getPropertyFactory().getConceptFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                startIndex += getPropertyFactory().getConceptFactories().getStoringFactory().getNumberOfColumns();
                final @Nonnull Time time = Time.STORING_FACTORY.restoreNonNullable(None.OBJECT, resultSet, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                final @Nonnull V value = getPropertyFactory().getValueFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                entries.add(TupleWrapper.encode(getStateType().getParameters().getNonNullable(0), concept, time, getPropertyFactory().getValueFactories().getEncodingFactory().encodeNonNullable(value)));
            }
            return ListWrapper.encode(getStateType(), entries.freeze());
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public void addState(@Nonnull E entity, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        assert block.getType().isBasedOn(getStateType()) : "The block is based on the state type of this data collection.";
        
        final @Nonnull String SQL = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + getName(entity.getSite()) + " (" + getPropertyFactory().getEntityFactories().getStoringFactory().getSelection() + ", " + getPropertyFactory().getConceptFactories().getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getSelection() + ") VALUES (" + getPropertyFactory().getEntityFactories().getStoringFactory().getInsertForPreparedStatement() + ", " + getPropertyFactory().getConceptFactories().getStoringFactory().getInsertForPreparedStatement() + ", " + Time.STORING_FACTORY.getInsertForPreparedStatement() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getInsertForPreparedStatement() + ")";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            final @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
            for (final @Nonnull Block entry : entries) {
                final @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                int startIndex = 0;
                
                getPropertyFactory().getEntityFactories().getStoringFactory().storeNonNullable(entity, preparedStatement, startIndex);
                startIndex += getPropertyFactory().getEntityFactories().getStoringFactory().getNumberOfColumns();
                
                final @Nonnull C concept = getPropertyFactory().getConceptFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(0));
                getPropertyFactory().getConceptFactories().getStoringFactory().storeNonNullable(concept, preparedStatement, startIndex);
                startIndex += getPropertyFactory().getConceptFactories().getStoringFactory().getNumberOfColumns();
                
                final @Nonnull Time time = Time.ENCODING_FACTORY.decodeNonNullable(None.OBJECT, elements.getNonNullable(1));
                Time.STORING_FACTORY.storeNonNullable(time, preparedStatement, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                
                final @Nonnull V value = getPropertyFactory().getValueFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(2));
                getPropertyFactory().getValueFactories().getStoringFactory().storeNonNullable(value, preparedStatement, startIndex);
                
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        getPropertyFactory().getConceptFactories().getStoringFactory().getIndex().reset(entity, this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Nonnull @NonNullableElements @Frozen ReadOnlyPair<Time, V> load(@Nonnull NonNullableConceptProperty<V, C, E> property) throws SQLException {
        final @Nonnull E entity = property.getConcept().getEntity();
        final @Nonnull String SQL = "SELECT " + Time.STORING_FACTORY.getSelection() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getSelection() + " FROM " + getName(entity.getSite()) + " WHERE " + getPropertyFactory().getEntityFactories().getStoringFactory().getConditionForStatement(entity) + " AND " + getPropertyFactory().getConceptFactories().getStoringFactory().getConditionForStatement(property.getConcept());
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                int startIndex = 0;
                final @Nonnull Time time = Time.STORING_FACTORY.restoreNonNullable(None.OBJECT, resultSet, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                final @Nonnull V value = getPropertyFactory().getValueFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                if (!property.getValueValidator().isValid(value)) throw new SQLException("The value of the given property is invalid.");
                return FreezablePair.get(time, value).freeze();
            } else {
                // TODO: Store the default value and return it.
                // TODO: What about changes 
                throw new SQLException("No value found for the given property.");
            }
        }
    }
    
    void replace(@Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull @Validated V oldValue, @Nonnull @Validated V newValue) throws SQLException {
        final @Nonnull E entity = property.getConcept().getEntity();
        final @Nonnull String SQL = "UPDATE " + getName(entity.getSite()) + " SET " + Time.STORING_FACTORY.getUpdateForStatement(newTime) + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getUpdateForPreparedStatement() + " WHERE " + getPropertyFactory().getEntityFactories().getStoringFactory().getConditionForStatement(entity) + " AND " + getPropertyFactory().getConceptFactories().getStoringFactory().getConditionForStatement(property.getConcept()) + " AND " + Time.STORING_FACTORY.getConditionForStatement(oldTime) + " AND " + getPropertyFactory().getValueFactories().getStoringFactory().getConditionForPreparedStatement();
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            int startIndex = 0;
            getPropertyFactory().getValueFactories().getStoringFactory().storeNonNullable(newValue, preparedStatement, startIndex);
            startIndex += getPropertyFactory().getValueFactories().getStoringFactory().getNumberOfColumns();
            getPropertyFactory().getValueFactories().getStoringFactory().storeNonNullable(oldValue, preparedStatement, startIndex);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The value of the given property could not be replaced.");
        }
    }
    
}
