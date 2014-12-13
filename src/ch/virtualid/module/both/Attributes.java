package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.attribute.Attribute;
import ch.virtualid.attribute.AttributeValue;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.EntityClass;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.expression.PassiveExpression;
import ch.virtualid.handler.InternalQuery;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.CoreService;
import ch.virtualid.server.Host;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyArray;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.SignatureWrapper;
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
 * @version 0.2
 */
public final class Attributes implements BothModule {
    
    public static final Attributes MODULE = new Attributes();
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "attribute_value (entity " + EntityClass.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, published BOOLEAN NOT NULL, value " + AttributeValue.FORMAT + " NOT NULL, PRIMARY KEY (entity, type, published), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "attribute_visibility (entity " + EntityClass.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, visibility TEXT NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", PRIMARY KEY (entity, type), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
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
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull Agent agent) throws SQLException {
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String from = " FROM " + site + "agent_permission p, " + site;
        final @Nonnull String where = " v WHERE p.entity = " + entity + " AND p.agent = " + agent + " AND v.entity = " + entity + " AND (p.type = " + AgentPermissions.GENERAL + " OR p.type = v.type)";
        
        final @Nonnull FreezableArray<Block> tables = new FreezableArray<Block>(2);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT DISTINCT v.type, v.published, v.value FROM" + from + "attribute_value" + where + " AND (v.published OR p.writing)")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 2);
                    final boolean published = resultSet.getBoolean(3);
                    final @Nonnull Block value = Block.get(AttributeValue.TYPE, resultSet, 4);
                    entries.add(new TupleWrapper(VALUE_STATE_ENTRY, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), new BooleanWrapper(AttributeValue.PUBLISHED, published), value.toBlockable()).toBlock());
                }
                tables.set(0, new ListWrapper(VALUE_STATE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT DISTINCT v.type, v.visibility FROM" + from + "attribute_visibility" + where + " AND p.writing")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    final @Nonnull Identity type = IdentityClass.getNotNull(resultSet, 2);
                    final @Nonnull String visibility = resultSet.getString(3);
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
                IdentityClass.create(elements.getNotNull(0)).toSemanticType().checkIsAttributeType().set(preparedStatement, 2);
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
                IdentityClass.create(elements.getNotNull(0)).toSemanticType().checkIsAttributeType().set(preparedStatement, 2);
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
    
    @Pure
    @Override
    public @Nullable InternalQuery getInternalQuery(@Nonnull Role role) {
        return null;
    }
    
    
    /**
     * Returns the value of the given attribute or null if no value is found.
     * 
     * @param attribute the attribute whose value is to be returned.
     * @param published whether the attribute is already published.
     * 
     * @return the value of the given attribute or null if no value is found.
     */
    public static @Nullable AttributeValue getValue(@Nonnull Attribute attribute, boolean published) throws SQLException {
        final @Nonnull String SQL = "SELECT value FROM " + attribute.getEntity().getSite() + "attribute_value WHERE entity = " + attribute.getEntity() + " AND type = " + attribute.getType() + " AND published = " + published;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return SignatureWrapper.decodeWithoutVerifying(Block.get(AttributeValue.TYPE, resultSet, 1), published, null);
            else return null;
        }
    }
    
    /**
     * Inserts the value of the attribute with the given type of the given entity with the given replacement policy.
     * 
     * @param entity the entity of the attribute whose value is to be inserted.
     * @param type the type of the attribute whose value is to be inserted.
     * @param published whether the value is to be published.
     * @param value a block of type {@code attribute@virtualid.ch}.
     * @param replace whether an existing value is to be replaced or not.
     */
    private static void insertValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published, @Nonnull Block value, boolean replace) throws SQLException {
        @Nonnull String statement = (replace ? "REPLACE" : "INSERT") + " INTO attribute_value (entity, type, published, value) VALUES (?, ?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setLong(1, entity.getNumber());
            preparedStatement.setLong(2, type.getNumber());
            preparedStatement.setBoolean(3, published);
            Database.setBlock(preparedStatement, 4, value);
            preparedStatement.executeUpdate();
        } catch (@Nonnull SQLException exception) {
            if (type.hasBeenMerged()) insertValue(connection, entity, type, published, value, replace);
            else throw exception;
        }
    }
    
    /**
     * Sets the value of the attribute with the given type of the given entity (by inserting a new entry or replacing an existing one).
     * 
     * @param entity the entity of the attribute whose value is to be set.
     * @param type the type of the attribute whose value is to be set.
     * @param published whether the value is to be published.
     * @param value a block of type {@code attribute@virtualid.ch}.
     */
    public static void setValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published, @Nonnull Block value) throws SQLException {
        insertValue(connection, entity, type, published, value, true);
    }
    
    /**
     * Adds the value of the attribute with the given type of the given entity or throws an {@link SQLException} if the value of the attribute is already set.
     * 
     * @param entity the entity of the attribute whose value is to be added.
     * @param type the type of the attribute whose value is to be added.
     * @param published whether the value is to be published.
     * @param value a block of type {@code attribute@virtualid.ch}.
     */
    public static void addValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published, @Nonnull Block value) throws SQLException {
        insertValue(connection, entity, type, published, value, false);
    }
    
    /**
     * Removes the value of the attribute with the given type of the given entity.
     * 
     * @param entity the entity of the attribute whose value is to be removed.
     * @param type the type of the attribute whose value is to be removed.
     * @param published whether to remove the published or unpublished value.
     */
    public static void removeValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM attribute_value WHERE entity = " + entity + " AND type = " + type + " AND published = " + published);
        }
    }
    
    /**
     * Removes the value of the attribute with the given type of the given entity or throws an {@link SQLException} if the attribute has a different value.
     * 
     * @param entity the entity of the attribute whose value is to be removed.
     * @param type the type of the attribute whose value is to be removed.
     * @param published whether to remove the published or unpublished value.
     * @param value a block of type {@code attribute@virtualid.ch}.
     */
    public static void removeValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published, @Nonnull Block value) throws SQLException {
        @Nonnull String statement = "DELETE FROM attribute_value WHERE entity = ? AND type = ? AND published = ? AND value = ?";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setLong(1, entity.getNumber());
            preparedStatement.setLong(2, type.getNumber());
            preparedStatement.setBoolean(3, published);
            Database.setBlock(preparedStatement, 4, value);
            if (preparedStatement.executeUpdate() == 0) {
                if (type.hasBeenMerged()) removeValue(connection, entity, type, published, value);
                else throw new SQLException("The value of the attribute with the type '" + type + "' of the entity '" + entity + "' could not be removed.");
            }
        }
    }
    
    /**
     * Replaces the value of the attribute with the given type of the given entity or throws an {@link SQLException} if it is not the old value of the attribute.
     * 
     * @param entity the entity of the attribute whose value is to be replaced.
     * @param type the type of the attribute whose value is to be removed.
     * @param published whether to remove the published or unpublished value.
     * @param oldValue the old value to be replaced by the new value.
     * @param newValue the new value by which the old value is replaced.
     */
    public static void replaceValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published, @Nonnull Block oldValue, @Nonnull Block newValue) throws SQLException {
        @Nonnull String statement = "UPDATE attribute_value SET value = ? WHERE entity = ? AND type = ? AND published = ? AND value = ?";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            Database.setBlock(preparedStatement, 1, newValue);
            preparedStatement.setLong(2, entity.getNumber());
            preparedStatement.setLong(3, type.getNumber());
            preparedStatement.setBoolean(4, published);
            Database.setBlock(preparedStatement, 5, oldValue);
            if (preparedStatement.executeUpdate() == 0) {
                if (type.hasBeenMerged()) replaceValue(connection, entity, type, published, oldValue, newValue);
                else throw new SQLException("The value of the attribute with the type '" + type + "' of the entity '" + entity + "' could not be replaced.");
            }
        }
    }
    
    
    /**
     * Returns the visibility of the attribute with the given type of the given entity or null if no such visibility is available.
     * 
     * @param entity the entity of the attribute whose visibility is to be returned.
     * @param type the type of the attribute whose visibility is to be returned.
     * @return the visibility of the attribute with the given type of the given entity or null if no such visibility is available.
     */
    public static @Nullable PassiveExpression getVisibility(@Nonnull Entity entity, @Nonnull SemanticType type) throws SQLException {
        @Nonnull String query = "SELECT visibility FROM attribute_visibility WHERE entity = " + entity + " AND type = " + type;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) return new PassiveExpression(resultSet.getString(1));
            else return null;
        }
    }
    
    /**
     * Adds the visibility of the attribute with the given type of the given entity or throws an {@link SQLException} if the visibility of the attribute is already set.
     * 
     * @param entity the entity of the attribute whose visibility is to be added.
     * @param type the type of the attribute whose visibility is to be added.
     * @param visibility the visibility of the attribute with the given type.
     */
    public static void addVisibility(@Nonnull Entity entity, @Nonnull SemanticType type, @Nonnull PassiveExpression visibility) throws SQLException {
        @Nonnull String statement = "INSERT INTO attribute_visibility (entity, type, visibility) VALUES (?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setLong(1, entity.getNumber());
            preparedStatement.setLong(2, type.getNumber());
            preparedStatement.setString(3, visibility.toString());
            preparedStatement.executeUpdate();
        } catch (@Nonnull SQLException exception) {
            if (type.hasBeenMerged()) addVisibility(connection, entity, type, visibility);
            else throw exception;
        }
    }
    
    /**
     * Removes the visibility of the attribute with the given type of the given entity or throws an {@link SQLException} if the attribute has a different visibility.
     * 
     * @param entity the entity of the attribute whose visibility is to be removed.
     * @param type the type of the attribute whose visibility is to be removed.
     * @param visibility the visibility of the attribute which is to be removed.
     */
    public static void removeVisibility(@Nonnull Entity entity, @Nonnull SemanticType type, @Nonnull PassiveExpression visibility) throws SQLException {
        @Nonnull String statement = "DELETE FROM attribute_visibility WHERE entity = ? AND type = ? AND visibility = ?";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setLong(1, entity.getNumber());
            preparedStatement.setLong(2, type.getNumber());
            preparedStatement.setString(3, visibility.toString());
            if (preparedStatement.executeUpdate() == 0) {
                if (type.hasBeenMerged()) removeVisibility(connection, entity, type, visibility);
                else throw new SQLException("The visibility of the attribute with the type '" + type + "' of the entity '" + entity + "' could not be removed.");
            }
        }
    }
    
    /**
     * Replaces the visibility of the attribute with the given type of the given entity or throws an {@link SQLException} if it is not the old visibility of the attribute.
     * 
     * @param entity the entity of the attribute whose visibility is to be replaced.
     * @param type the type of the attribute whose visibility is to be removed.
     * @param oldVisibility the old visibility to be replaced by the new visibility.
     * @param newVisibility the new visibility by which the old visibility is replaced.
     */
    public static void replaceVisibility(@Nonnull Entity entity, @Nonnull SemanticType type, @Nonnull PassiveExpression oldVisibility, @Nonnull PassiveExpression newVisibility) throws SQLException {
        @Nonnull String statement = "UPDATE attribute_visibility SET visibility = ? WHERE entity = ? AND type = ? AND visibility = ?";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setString(1, newVisibility.toString());
            preparedStatement.setLong(2, entity.getNumber());
            preparedStatement.setLong(3, type.getNumber());
            preparedStatement.setString(4, oldVisibility.toString());
            if (preparedStatement.executeUpdate() == 0) {
                if (type.hasBeenMerged()) replaceVisibility(connection, entity, type, oldVisibility, newVisibility);
                else throw new SQLException("The visibility of the attribute with the type '" + type + "' of the entity '" + entity + "' could not be replaced.");
            }
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
