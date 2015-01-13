package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.attribute.Attribute;
import ch.virtualid.attribute.AttributeValue;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.EntityClass;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.expression.PassiveExpression;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.InternalPerson;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.server.Host;
import ch.virtualid.service.CoreService;
import ch.virtualid.service.Service;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableLinkedHashSet;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.FreezableSet;
import ch.virtualid.util.ReadonlyArray;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides database access to the {@link Attribute attributes} of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Attributes implements BothModule {
    
    /**
     * Stores an instance of this module.
     */
    public static final Attributes MODULE = new Attributes();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "attribute_value (entity " + EntityClass.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, published BOOLEAN NOT NULL, value " + AttributeValue.FORMAT + " NOT NULL, PRIMARY KEY (entity, type, published), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "attribute_visibility (entity " + EntityClass.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, visibility " + PassiveExpression.FORMAT + ", PRIMARY KEY (entity, type), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "attribute_visibility");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "attribute_value");
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.value.attributes.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType VALUE_MODULE_ENTRY = SemanticType.create("entry.value.attributes.module@virtualid.ch").load(TupleWrapper.TYPE, Identity.IDENTIFIER, SemanticType.ATTRIBUTE_IDENTIFIER, AttributeValue.PUBLISHED, AttributeValue.TYPE);
    
    /**
     * Stores the semantic type {@code table.value.attributes.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType VALUE_MODULE_TABLE = SemanticType.create("table.value.attributes.module@virtualid.ch").load(ListWrapper.TYPE, VALUE_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.visibility.attributes.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType VISIBILITY_MODULE_ENTRY = SemanticType.create("entry.visibility.attributes.module@virtualid.ch").load(TupleWrapper.TYPE, Identity.IDENTIFIER, SemanticType.ATTRIBUTE_IDENTIFIER, PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code table.visibility.attributes.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType VISIBILITY_MODULE_TABLE = SemanticType.create("table.visibility.attributes.module@virtualid.ch").load(ListWrapper.TYPE, VISIBILITY_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code attributes.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.create("attributes.module@virtualid.ch").load(TupleWrapper.TYPE, VALUE_MODULE_TABLE, VISIBILITY_MODULE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableArray<Block> tables = new FreezableArray<Block>(2);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, type, published, value FROM " + host + "attribute_value")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, 1);
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 2);
                    final boolean published = resultSet.getBoolean(3);
                    final @Nonnull Block value = Block.get(AttributeValue.TYPE, resultSet, 4);
                    entries.add(new TupleWrapper(VALUE_MODULE_ENTRY, identity, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), new BooleanWrapper(AttributeValue.PUBLISHED, published), value.toBlockable()).toBlock());
                }
                tables.set(0, new ListWrapper(VALUE_MODULE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, type, visibility FROM " + host + "attribute_visibility")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, 1);
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 2);
                    final @Nonnull String visibility = resultSet.getString(3);
                    entries.add(new TupleWrapper(VISIBILITY_MODULE_ENTRY, identity, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), new StringWrapper(PassiveExpression.TYPE, visibility)).toBlock());
                }
                tables.set(1, new ListWrapper(VISIBILITY_MODULE_TABLE, entries.freeze()).toBlock());
            }
            
        }
        return new TupleWrapper(MODULE_FORMAT, tables.freeze()).toBlock();
    }
    
    @Override
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadonlyArray<Block> tables = new TupleWrapper(block).getElementsNotNull(2);
        final @Nonnull String prefix = "INSERT INTO " + host;
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "attribute_value (entity, type, published, value) VALUES (?, ?, ?, ?)")) {
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(0)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(4);
                IdentityClass.create(elements.getNotNull(0)).toInternalIdentity().set(preparedStatement, 1);
                IdentityClass.create(elements.getNotNull(1)).toSemanticType().checkIsAttributeType().set(preparedStatement, 2);
                preparedStatement.setBoolean(3, new BooleanWrapper(elements.getNotNull(2)).getValue());
                elements.getNotNull(3).set(preparedStatement, 4);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "attribute_visibility (entity, type, visibility) VALUES (?, ?, ?)")) {
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(1)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(3);
                IdentityClass.create(elements.getNotNull(0)).toInternalIdentity().set(preparedStatement, 1);
                IdentityClass.create(elements.getNotNull(1)).toSemanticType().checkIsAttributeType().set(preparedStatement, 2);
                preparedStatement.setString(2, new StringWrapper(elements.getNotNull(2)).getString());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.value.attributes.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType VALUE_STATE_ENTRY = SemanticType.create("entry.value.attributes.state@virtualid.ch").load(TupleWrapper.TYPE, SemanticType.ATTRIBUTE_IDENTIFIER, AttributeValue.PUBLISHED, AttributeValue.TYPE);
    
    /**
     * Stores the semantic type {@code table.value.attributes.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType VALUE_STATE_TABLE = SemanticType.create("table.value.attributes.state@virtualid.ch").load(ListWrapper.TYPE, VALUE_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.visibility.attributes.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType VISIBILITY_STATE_ENTRY = SemanticType.create("entry.visibility.attributes.state@virtualid.ch").load(TupleWrapper.TYPE, SemanticType.ATTRIBUTE_IDENTIFIER, PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code table.visibility.attributes.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType VISIBILITY_STATE_TABLE = SemanticType.create("table.visibility.attributes.state@virtualid.ch").load(ListWrapper.TYPE, VISIBILITY_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code attributes.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.create("attributes.state@virtualid.ch").load(TupleWrapper.TYPE, VALUE_STATE_TABLE, VISIBILITY_STATE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadonlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        final @Nonnull Site site = entity.getSite();
        final @Nonnull FreezableArray<Block> tables = new FreezableArray<Block>(2);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT DISTINCT type, published, value FROM " + site + "attribute_value WHERE entity = " + entity + (permissions.canRead(AgentPermissions.GENERAL) ? "" : " AND type IN " + permissions.allTypesToString()))) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 1);
                    final boolean published = resultSet.getBoolean(2);
                    final @Nonnull Block value = Block.get(AttributeValue.TYPE, resultSet, 3);
                    entries.add(new TupleWrapper(VALUE_STATE_ENTRY, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), new BooleanWrapper(AttributeValue.PUBLISHED, published), value.toBlockable()).toBlock());
                }
                tables.set(0, new ListWrapper(VALUE_STATE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT DISTINCT type, visibility FROM " + site + "attribute_visibility WHERE entity = " + entity + (permissions.canWrite(AgentPermissions.GENERAL) ? "" : " AND type IN " + permissions.writeTypesToString()))) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 1);
                    final @Nonnull String visibility = resultSet.getString(2);
                    entries.add(new TupleWrapper(VISIBILITY_STATE_ENTRY, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), new StringWrapper(PassiveExpression.TYPE, visibility)).toBlock());
                }
                tables.set(1, new ListWrapper(VISIBILITY_STATE_TABLE, entries.freeze()).toBlock());
            }
            
        }
        return new TupleWrapper(STATE_FORMAT, tables.freeze()).toBlock();
    }
    
    @Override
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertIgnore(statement, site + "attribute_value", "entity", "type", "published");
            Database.onInsertIgnore(statement, site + "attribute_visibility", "entity", "type");
        }
        
        final @Nonnull ReadonlyArray<Block> tables = new TupleWrapper(block).getElementsNotNull(2);
        final @Nonnull String prefix = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + site;
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "attribute_value (entity, type, published, value) VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(0)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(3);
                IdentityClass.create(elements.getNotNull(0)).toSemanticType().checkIsAttributeFor(entity).set(preparedStatement, 2);
                preparedStatement.setBoolean(3, new BooleanWrapper(elements.getNotNull(1)).getValue());
                elements.getNotNull(2).set(preparedStatement, 4);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(prefix + "attribute_visibility (entity, type, visibility) VALUES (?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(1)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(2);
                IdentityClass.create(elements.getNotNull(0)).toSemanticType().checkIsAttributeFor(entity).set(preparedStatement, 2);
                preparedStatement.setString(2, new StringWrapper(elements.getNotNull(1)).getString());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotIgnore(statement, site + "attribute_value");
            Database.onInsertNotIgnore(statement, site + "attribute_visibility");
        }
        
        Attribute.reset(entity);
    }
    
    @Override
    public void removeState(@Nonnull NonHostEntity entity) throws SQLException {
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + site + "attribute_visibility WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "attribute_value WHERE entity = " + entity);
        }
    }
    
    
    /**
     * Returns all the attributes of the given entity.
     * 
     * @param entity the entity whose attributes are to be returned.
     * 
     * @return all the attributes of the given entity.
     * 
     * @ensure return.isNotFrozen() : "The returned attributes are not frozen.";
     */
    @Pure
    public static @Capturable @Nonnull FreezableSet<Attribute> getAll(@Nonnull Entity entity) throws SQLException {
        final @Nonnull String SQL = "SELECT type FROM " + entity.getSite() + "attribute_value WHERE entity = " + entity;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableSet<Attribute> attributes = new FreezableLinkedHashSet<Attribute>();
            while (resultSet.next()) attributes.add(Attribute.get(entity, IdentityClass.getNotNull(resultSet, 1).toSemanticType().checkIsAttributeFor(entity)));
            return attributes;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    
    /**
     * Returns the value of the given attribute or null if no value is found.
     * 
     * @param attribute the attribute whose value is to be returned.
     * @param published whether the attribute is already published.
     * 
     * @return the value of the given attribute or null if no value is found.
     * 
     * @ensure return == null || return.isVerified() && return.matches(attribute) : "The returned value is null or verified and matches the given attribute.";
     */
    @Pure
    public static @Nullable AttributeValue getValue(@Nonnull Attribute attribute, boolean published) throws SQLException {
        final @Nonnull String SQL = "SELECT value FROM " + attribute.getEntity().getSite() + "attribute_value WHERE entity = " + attribute.getEntity() + " AND type = " + attribute.getType() + " AND published = " + published;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return AttributeValue.get(resultSet, 1).checkMatches(attribute);
            else return null;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Inserts the given value for the given attribute.
     * 
     * @param attribute the attribute for which the value is to be inserted.
     * @param published whether the published value is to be inserted.
     * @param value the value which is to be inserted for the attribute.
     * 
     * @require value.isVerified() && value.matches(attribute) : "The value is verified and matches the given attribute.";
     */
    public static void insertValue(@Nonnull Attribute attribute, boolean published, @Nonnull AttributeValue value) throws SQLException {
        assert value.isVerified() && value.matches(attribute) : "The value is verified and matches the given attribute.";
        
        final @Nonnull String SQL = "INSERT INTO " + attribute.getEntity().getSite() + "attribute_value (entity, type, published, value) VALUES (?, ?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            attribute.getEntity().set(preparedStatement, 1);
            attribute.getType().set(preparedStatement, 2);
            preparedStatement.setBoolean(3, published);
            value.set(preparedStatement, 4);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Deletes the given value from the given attribute.
     * 
     * @param attribute the attribute whose value is to be deleted.
     * @param published whether the published value is to be deleted.
     * @param value the value which is to be deleted from the attribute.
     * 
     * @require value.isVerified() && value.matches(attribute) : "The value is verified and matches the given attribute.";
     */
    public static void deleteValue(@Nonnull Attribute attribute, boolean published, @Nonnull AttributeValue value) throws SQLException {
        assert value.isVerified() && value.matches(attribute) : "The value is verified and matches the given attribute.";
        
        final @Nonnull String SQL = "DELETE FROM " + attribute.getEntity().getSite() + "attribute_value WHERE entity = ? AND type = ? AND published = ? AND value = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            attribute.getEntity().set(preparedStatement, 1);
            attribute.getType().set(preparedStatement, 2);
            preparedStatement.setBoolean(3, published);
            value.set(preparedStatement, 4);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The value of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be deleted.");
        }
    }
    
    /**
     * Replaces the value of the given attribute.
     * 
     * @param attribute the attribute whose value is to be replaced.
     * @param published whether to replace the published or unpublished value.
     * @param oldValue the old value to be replaced by the new value.
     * @param newValue the new value by which the old value is replaced.
     * 
     * @require oldValue.isVerified() && oldValue.matches(attribute) : "The old value is verified and matches the given attribute.";
     * @require newValue.isVerified() && newValue.matches(attribute) : "The new value is verified and matches the given attribute.";
     */
    public static void replaceValue(@Nonnull Attribute attribute, boolean published, @Nonnull AttributeValue oldValue, @Nonnull AttributeValue newValue) throws SQLException {
        assert oldValue.isVerified() && oldValue.matches(attribute) : "The old value is verified and matches the given attribute.";
        assert newValue.isVerified() && newValue.matches(attribute) : "The new value is verified and matches the given attribute.";
        
        final @Nonnull String SQL = "UPDATE " + attribute.getEntity().getSite() + "attribute_value SET value = ? WHERE entity = ? AND type = ? AND published = ? AND value = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            newValue.set(preparedStatement, 1);
            attribute.getEntity().set(preparedStatement, 2);
            attribute.getType().set(preparedStatement, 3);
            preparedStatement.setBoolean(4, published);
            oldValue.set(preparedStatement, 5);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The value of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be replaced.");
        }
    }
    
    
    /**
     * Returns the visibility of the given attribute or null if no visibility is found.
     * 
     * @param attribute the attribute whose visibility is to be returned.
     * 
     * @return the visibility of the attribute with the given type of the given entity or null if no such visibility is available.
     * 
     * @require attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
     * 
     * @ensure return == null || return.getEntity().equals(attribute.getEntity()) : "The returned visibility is null or belongs to the entity of the given attribute.";
     */
    @Pure
    public static @Nullable PassiveExpression getVisibility(@Nonnull Attribute attribute) throws SQLException {
        assert attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
        
        final @Nonnull String SQL = "SELECT visibility FROM " + attribute.getEntity().getSite() + "attribute_visibility WHERE entity = " + attribute.getEntity() + " AND type = " + attribute.getType();
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return PassiveExpression.get((NonHostEntity) attribute.getEntity(), resultSet, 1);
            else return null;
        }
    }
    
    /**
     * Inserts the given visibility for the given attribute.
     * 
     * @param attribute the attribute for which the visibility is to be inserted.
     * @param visibility the visibility which is to be inserted for the attribute.
     * 
     * @require attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
     * @require visibility.getEntity().equals(attribute.getEntity()) : "The visibility and the attribute belong to the same entity.";
     */
    public static void insertVisibility(@Nonnull Attribute attribute, @Nonnull PassiveExpression visibility) throws SQLException {
        assert attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
        assert visibility.getEntity().equals(attribute.getEntity()) : "The visibility and the attribute belong to the same entity.";
        
        final @Nonnull String SQL = "INSERT INTO " + attribute.getEntity().getSite() + "attribute_visibility (entity, type, visibility) VALUES (?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            attribute.getEntity().set(preparedStatement, 1);
            attribute.getType().set(preparedStatement, 2);
            visibility.set(preparedStatement, 3);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Deletes the given visibility from the given attribute.
     * 
     * @param attribute the attribute whose visibility is to be deleted.
     * @param visibility the visibility which is to be deleted from the attribute.
     * 
     * @require attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
     * @require visibility.getEntity().equals(attribute.getEntity()) : "The visibility and the attribute belong to the same entity.";
     */
    public static void deleteVisibility(@Nonnull Attribute attribute, @Nonnull PassiveExpression visibility) throws SQLException {
        assert attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
        assert visibility.getEntity().equals(attribute.getEntity()) : "The visibility and the attribute belong to the same entity.";
        
        final @Nonnull String SQL = "DELETE FROM " + attribute.getEntity().getSite() + "attribute_visibility WHERE entity = ? AND type = ? AND visibility = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            attribute.getEntity().set(preparedStatement, 1);
            attribute.getType().set(preparedStatement, 2);
            visibility.set(preparedStatement, 3);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The visibility of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be deleted.");
        }
    }
    
    /**
     * Replaces the visibility of the given attribute.
     * 
     * @param attribute the attribute whose visibility is to be replaced.
     * @param oldVisibility the old visibility to be replaced by the new visibility.
     * @param newVisibility the new visibility by which the old visibility is replaced.
     * 
     * @require attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
     * @require oldVisibility.getEntity().equals(attribute.getEntity()) : "The old visibility and the attribute belong to the same entity.";
     * @require newVisibility.getEntity().equals(attribute.getEntity()) : "The new visibility and the attribute belong to the same entity.";
     */
    public static void replaceVisibility(@Nonnull Attribute attribute, @Nonnull PassiveExpression oldVisibility, @Nonnull PassiveExpression newVisibility) throws SQLException {
        assert attribute.getEntity().getIdentity() instanceof InternalPerson : "The entity of the given attribute belongs to an internal person.";
        assert oldVisibility.getEntity().equals(attribute.getEntity()) : "The old visibility and the attribute belong to the same entity.";
        assert newVisibility.getEntity().equals(attribute.getEntity()) : "The new visibility and the attribute belong to the same entity.";
        
        final @Nonnull String SQL = "UPDATE " + attribute.getEntity().getSite() + "attribute_visibility SET visibility = ? WHERE entity = ? AND type = ? AND visibility = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            newVisibility.set(preparedStatement, 1);
            attribute.getEntity().set(preparedStatement, 2);
            attribute.getType().set(preparedStatement, 3);
            oldVisibility.set(preparedStatement, 4);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The visibility of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be replaced.");
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
