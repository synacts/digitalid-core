package net.digitalid.service.core.concept.property.nonnullable;

import net.digitalid.service.core.property.ReadOnlyProperty;
import net.digitalid.service.core.factory.ConceptFactories;
import net.digitalid.service.core.factory.Factories;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.concept.property.ConceptPropertyTable;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.site.host.Host;
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
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.site.Site;

/**
 * This class models a database table that stores a non-nullable {@link ReadOnlyProperty property} of a {@link Concept concept}.
 */
@Immutable
public final class NonNullableConceptPropertyTable<V, C extends Concept<C, E, ?>, E extends Entity<E>> extends ConceptPropertyTable<V, C, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new non-nullable property table with the given configuration from the property setup.
     * 
     * @param propertySetup
     */
    @OnMainThread
    NonNullableConceptPropertyTable(@Nonnull NonNullableConceptPropertySetup<V, C, E> propertySetup) {
        super(propertySetup);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– ClientTable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Locked
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Factories<E, Site> entityFactories = getPropertyFactory().getConceptSetup().getEntityFactories();
            ConceptFactories<C, E> conceptFactories = getPropertyFactory().getConceptSetup().getConceptFactories();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + getName(site) + " (" + entityFactories.getStoringFactory().getDeclaration() + ", " + conceptFactories.getStoringFactory().getDeclaration() + ", " + Time.STORING_FACTORY.getDeclaration() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getDeclaration() + ", PRIMARY KEY (" + entityFactories.getStoringFactory().getSelection() + ", " + conceptFactories.getStoringFactory().getSelection() + ")" + entityFactories.getStoringFactory().getForeignKeys(site) + conceptFactories.getStoringFactory().getForeignKeys(site) + getPropertyFactory().getValueFactories().getStoringFactory().getForeignKeys(site) + ")");
            Database.onInsertIgnore(statement, getName(site), entityFactories.getStoringFactory().getSelection(), conceptFactories.getStoringFactory().getSelection()); // TODO: There is a problem when the entity or the concept uses more than one column because the onInsertIgnore-method expects the arguments differently.
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
    public @Nonnull Block exportAll(@Nonnull Host host) throws AbortException {
        Factories<E, Site> entityFactories = getPropertyFactory().getConceptSetup().getEntityFactories();
        ConceptFactories<C, E> conceptFactories = getPropertyFactory().getConceptSetup().getConceptFactories();
        final @Nonnull String SQL = "SELECT " + entityFactories.getStoringFactory().getSelection() + ", " + conceptFactories.getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getSelection() + " FROM " + getName(host);
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = FreezableLinkedList.get();
            while (resultSet.next()) {
                int startIndex = 0;
                final @Nonnull E entity = entityFactories.getStoringFactory().restoreNonNullable(host, resultSet, startIndex);
                startIndex += entityFactories.getStoringFactory().getNumberOfColumns();
                final @Nonnull C concept = conceptFactories.getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                startIndex += conceptFactories.getStoringFactory().getNumberOfColumns();
                final @Nonnull Time time = Time.STORING_FACTORY.restoreNonNullable(None.OBJECT, resultSet, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                final @Nonnull V value = getPropertyFactory().getValueFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                entries.add(TupleWrapper.encode(getDumpType().getParameters().getNonNullable(0), entity, concept, time, getPropertyFactory().getValueFactories().getEncodingFactory().encodeNonNullable(value)));
            }
            return ListWrapper.encode(getDumpType(), entries.freeze());
        } catch (SQLException exception) {
            throw AbortException.get(exception);
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public void importAll(@Nonnull Host host, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        assert block.getType().isBasedOn(getDumpType()) : "The block is based on the dump type of this data collection.";
        
        Factories<E, Site> entityFactories = getPropertyFactory().getConceptSetup().getEntityFactories();
        ConceptFactories<C, E> conceptFactories = getPropertyFactory().getConceptSetup().getConceptFactories();
        final @Nonnull String SQL = "INSERT INTO " + getName(host) + " (" + entityFactories.getStoringFactory().getSelection() + ", " + conceptFactories.getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getSelection() + ") VALUES (" + entityFactories.getStoringFactory().getInsertForPreparedStatement() + ", " + conceptFactories.getStoringFactory().getInsertForPreparedStatement() + ", " + Time.STORING_FACTORY.getInsertForPreparedStatement() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getInsertForPreparedStatement() + ")";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            final @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
            for (final @Nonnull Block entry : entries) {
                final @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(4);
                int startIndex = 0;
                
                final @Nonnull E entity = entityFactories.getEncodingFactory().decodeNonNullable(host, elements.getNonNullable(0));
                entityFactories.getStoringFactory().storeNonNullable(entity, preparedStatement, startIndex);
                startIndex += entityFactories.getStoringFactory().getNumberOfColumns();
                
                final @Nonnull C concept = conceptFactories.getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(1));
                conceptFactories.getStoringFactory().storeNonNullable(concept, preparedStatement, startIndex);
                startIndex += conceptFactories.getStoringFactory().getNumberOfColumns();
                
                final @Nonnull Time time = Time.ENCODING_FACTORY.decodeNonNullable(None.OBJECT, elements.getNonNullable(2));
                Time.STORING_FACTORY.storeNonNullable(time, preparedStatement, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                
                final @Nonnull V value = getPropertyFactory().getValueFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(3));
                getPropertyFactory().getValueFactories().getStoringFactory().storeNonNullable(value, preparedStatement, startIndex);
                
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException exception) {
            throw AbortException.get(exception);
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– StateTable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull E entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws AbortException {
        Factories<E, Site> entityFactories = getPropertyFactory().getConceptSetup().getEntityFactories();
        ConceptFactories<C, E> conceptFactories = getPropertyFactory().getConceptSetup().getConceptFactories();
        // TODO: String SQL = select(getConceptFactories(), Time.FACTORIES, getPropertyFactory().getValueFactories()).from(entity).where(factory, object).and().and().toSQL();
        // TODO: Instead of conceptFactories.getStoringFactory().storeNonNullable and conceptFactories.getStoringFactory().restoreNonNullable, one could define a store and restore method that also takes a GeneralFactories as a parameter.
        final @Nonnull String SQL = "SELECT " + conceptFactories.getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getSelection() + " FROM " + getName(entity.getSite()) + " WHERE " + entityFactories.getStoringFactory().getConditionForStatement(entity) + " AND " + getPropertyFactory().getRequiredAuthorization().getStateFilter(permissions, restrictions, agent);
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = FreezableLinkedList.get();
            while (resultSet.next()) {
                int startIndex = 0;
                final @Nonnull C concept = conceptFactories.getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                startIndex += conceptFactories.getStoringFactory().getNumberOfColumns();
                final @Nonnull Time time = Time.STORING_FACTORY.restoreNonNullable(None.OBJECT, resultSet, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                final @Nonnull V value = getPropertyFactory().getValueFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                entries.add(TupleWrapper.encode(getStateType().getParameters().getNonNullable(0), concept, time, getPropertyFactory().getValueFactories().getEncodingFactory().encodeNonNullable(value)));
            }
            return ListWrapper.encode(getStateType(), entries.freeze());
        } catch (SQLException exception) {
            throw AbortException.get(exception);
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public void addState(@Nonnull E entity, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        assert block.getType().isBasedOn(getStateType()) : "The block is based on the state type of this data collection.";
        
        Factories<E, Site> entityFactories = getPropertyFactory().getConceptSetup().getEntityFactories();
        ConceptFactories<C, E> conceptFactories = getPropertyFactory().getConceptSetup().getConceptFactories();
        final @Nonnull String SQL = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + getName(entity.getSite()) + " (" + entityFactories.getStoringFactory().getSelection() + ", " + conceptFactories.getStoringFactory().getSelection() + ", " + Time.STORING_FACTORY.getSelection() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getSelection() + ") VALUES (" + entityFactories.getStoringFactory().getInsertForPreparedStatement() + ", " + conceptFactories.getStoringFactory().getInsertForPreparedStatement() + ", " + Time.STORING_FACTORY.getInsertForPreparedStatement() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getInsertForPreparedStatement() + ")";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            final @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
            for (final @Nonnull Block entry : entries) {
                final @Nonnull @NonNullableElements @Frozen ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                int startIndex = 0;
                
                entityFactories.getStoringFactory().storeNonNullable(entity, preparedStatement, startIndex);
                startIndex += entityFactories.getStoringFactory().getNumberOfColumns();
                
                final @Nonnull C concept = conceptFactories.getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(0));
                conceptFactories.getStoringFactory().storeNonNullable(concept, preparedStatement, startIndex);
                startIndex += conceptFactories.getStoringFactory().getNumberOfColumns();
                
                final @Nonnull Time time = Time.ENCODING_FACTORY.decodeNonNullable(None.OBJECT, elements.getNonNullable(1));
                Time.STORING_FACTORY.storeNonNullable(time, preparedStatement, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                
                final @Nonnull V value = getPropertyFactory().getValueFactories().getEncodingFactory().decodeNonNullable(entity, elements.getNonNullable(2));
                getPropertyFactory().getValueFactories().getStoringFactory().storeNonNullable(value, preparedStatement, startIndex);
                
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException exception) {
            throw AbortException.get(exception);
        }
        
        conceptFactories.getStoringFactory().getIndex().reset(entity, this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Nonnull @NonNullableElements @Frozen ReadOnlyPair<Time, V> load(@Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull NonNullableConceptPropertySetup<V, C, E> propertySetup) throws AbortException {
        Factories<E, Site> entityFactories = getPropertyFactory().getConceptSetup().getEntityFactories();
        ConceptFactories<C, E> conceptFactories = getPropertyFactory().getConceptSetup().getConceptFactories();
        final @Nonnull E entity = property.getConcept().getEntity();
        final @Nonnull String SQL = "SELECT " + Time.STORING_FACTORY.getSelection() + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getSelection() + " FROM " + getName(entity.getSite()) + " WHERE " + entityFactories.getStoringFactory().getConditionForStatement(entity) + " AND " + conceptFactories.getStoringFactory().getConditionForStatement(property.getConcept());
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                int startIndex = 0;
                final @Nonnull Time time = Time.STORING_FACTORY.restoreNonNullable(None.OBJECT, resultSet, startIndex);
                startIndex += Time.STORING_FACTORY.getNumberOfColumns();
                final @Nonnull V value = getPropertyFactory().getValueFactories().getStoringFactory().restoreNonNullable(entity, resultSet, startIndex);
                if (!property.getValueValidator().isValid(value)) throw new SQLException("The value of the given property is invalid.");
                return FreezablePair.get(time, value).freeze();
            } else {
                // TODO: What about changes
                return FreezablePair.get(Time.getCurrent(), propertySetup.getDefaultValue()).freeze();
            }
        } catch (SQLException exception) {
            throw AbortException.get(exception);
        }
    }
    
    void replace(@Nonnull NonNullableConceptProperty<V, C, E> property, @Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull @Validated V oldValue, @Nonnull @Validated V newValue) throws AbortException {
        Factories<E, Site> entityFactories = getPropertyFactory().getConceptSetup().getEntityFactories();
        ConceptFactories<C, E> conceptFactories = getPropertyFactory().getConceptSetup().getConceptFactories();
        final @Nonnull E entity = property.getConcept().getEntity();
        final @Nonnull String SQL = "UPDATE " + getName(entity.getSite()) + " SET " + Time.STORING_FACTORY.getUpdateForStatement(newTime) + ", " + getPropertyFactory().getValueFactories().getStoringFactory().getUpdateForPreparedStatement() + " WHERE " + entityFactories.getStoringFactory().getConditionForStatement(entity) + " AND " + conceptFactories.getStoringFactory().getConditionForStatement(property.getConcept()) + " AND " + Time.STORING_FACTORY.getConditionForStatement(oldTime) + " AND " + getPropertyFactory().getValueFactories().getStoringFactory().getConditionForPreparedStatement();
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            int startIndex = 0;
            getPropertyFactory().getValueFactories().getStoringFactory().storeNonNullable(newValue, preparedStatement, startIndex);
            startIndex += getPropertyFactory().getValueFactories().getStoringFactory().getNumberOfColumns();
            getPropertyFactory().getValueFactories().getStoringFactory().storeNonNullable(oldValue, preparedStatement, startIndex);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The value of the given property could not be replaced.");
        } catch (SQLException exception) {
            throw AbortException.get(exception);
        }
    }
    
}
