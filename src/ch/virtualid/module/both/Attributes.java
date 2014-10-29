package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concepts.Attribute;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.handler.InternalQuery;
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
 * This class provides database access to the {@link Attribute attributes} of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.2
 */
public final class Attributes implements BothModule {
    
    static { CoreService.SERVICE.add(new Attributes()); }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (final @Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS attribute_value (entity BIGINT NOT NULL, type BIGINT NOT NULL, published BOOLEAN NOT NULL, value LONGBLOB NOT NULL, PRIMARY KEY (entity, type, published), FOREIGN KEY (entity) REFERENCES " + connection.getReference() + ", FOREIGN KEY (type) REFERENCES general_identity (identity))");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS attribute_visibility (entity BIGINT NOT NULL, type BIGINT NOT NULL, visibility TEXT NOT NULL COLLATE " + Database.UTF16_BIN + ", PRIMARY KEY (entity, type), FOREIGN KEY (entity) REFERENCES " + connection.getReference() + ", FOREIGN KEY (type) REFERENCES general_identity (identity))");
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (final @Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.attributes.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.create("entry.attributes.module@virtualid.ch").load(TupleWrapper.TYPE, ch.virtualid.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code attributes.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.create("attributes.module@virtualid.ch").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE;
    }
    
    @Pure
    @Override
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
        try (final @Nonnull Statement statement = Database.createStatement()) {
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
     * Stores the semantic type {@code entry.attributes.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE_ENTRY = SemanticType.create("entry.attributes.state@virtualid.ch").load(TupleWrapper.TYPE, ch.virtualid.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code attributes.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.create("attributes.state@virtualid.ch").load(ListWrapper.TYPE, STATE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE;
    }
    
    @Pure
    @Override
    public @Nonnull Block getState(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
        try (final @Nonnull Statement statement = Database.createStatement()) {
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
        try (final @Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("DELETE FROM attribute_value WHERE entity = " + entity);
//            statement.executeUpdate("DELETE FROM attribute_visibility WHERE entity = " + entity);
        }
    }
    
    @Pure
    @Override
    public @Nullable InternalQuery getInternalQuery(@Nonnull Role role) {
        return null; // TODO: Return the internal query for reloading the data of this module.
    }
    
    
//    /**
//     * Returns the value of the given attribute or null if no such value is available.
//     * 
//     * @param attribute the attribute whose value is to be returned.
//     * @param published whether the attribute is already published.
//     * @return the value (a block of type {@code attribute@virtualid.ch}) of the given attribute or null if no such value is available.
//     */
//    public static @Nullable Block getValue(@Nonnull Attribute attribute, boolean published) throws SQLException {
//        @Nonnull String query = "SELECT value FROM attribute_value WHERE entity = " + attribute.getEntityNotNull() + " AND type = " + attribute.getType() + " AND published = " + published;
//        try (@Nonnull Statement statement = attribute.getConnection().createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            if (resultSet.next()) return new Block(resultSet.getBytes(1));
//            else return null;
//        }
//    }
//    
//    /**
//     * Inserts the value of the attribute with the given type of the given entity with the given replacement policy.
//     * 
//     * @param entity the entity of the attribute whose value is to be inserted.
//     * @param type the type of the attribute whose value is to be inserted.
//     * @param published whether the value is to be published.
//     * @param value a block of type {@code attribute@virtualid.ch}.
//     * @param replace whether an existing value is to be replaced or not.
//     */
//    private static void insertValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published, @Nonnull Block value, boolean replace) throws SQLException {
//        @Nonnull String statement = (replace ? "REPLACE" : "INSERT") + " INTO attribute_value (entity, type, published, value) VALUES (?, ?, ?, ?)";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
//            preparedStatement.setLong(1, entity.getNumber());
//            preparedStatement.setLong(2, type.getNumber());
//            preparedStatement.setBoolean(3, published);
//            Database.setBlock(preparedStatement, 4, value);
//            preparedStatement.executeUpdate();
//        } catch (@Nonnull SQLException exception) {
//            if (type.hasBeenMerged()) insertValue(connection, entity, type, published, value, replace);
//            else throw exception;
//        }
//    }
//    
//    /**
//     * Sets the value of the attribute with the given type of the given entity (by inserting a new entry or replacing an existing one).
//     * 
//     * @param entity the entity of the attribute whose value is to be set.
//     * @param type the type of the attribute whose value is to be set.
//     * @param published whether the value is to be published.
//     * @param value a block of type {@code attribute@virtualid.ch}.
//     */
//    public static void setValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published, @Nonnull Block value) throws SQLException {
//        insertValue(connection, entity, type, published, value, true);
//    }
//    
//    /**
//     * Adds the value of the attribute with the given type of the given entity or throws an {@link SQLException} if the value of the attribute is already set.
//     * 
//     * @param entity the entity of the attribute whose value is to be added.
//     * @param type the type of the attribute whose value is to be added.
//     * @param published whether the value is to be published.
//     * @param value a block of type {@code attribute@virtualid.ch}.
//     */
//    public static void addValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published, @Nonnull Block value) throws SQLException {
//        insertValue(connection, entity, type, published, value, false);
//    }
//    
//    /**
//     * Removes the value of the attribute with the given type of the given entity.
//     * 
//     * @param entity the entity of the attribute whose value is to be removed.
//     * @param type the type of the attribute whose value is to be removed.
//     * @param published whether to remove the published or unpublished value.
//     */
//    public static void removeValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published) throws SQLException {
//        try (@Nonnull Statement statement = connection.createStatement()) {
//            statement.executeUpdate("DELETE FROM attribute_value WHERE entity = " + entity + " AND type = " + type + " AND published = " + published);
//        }
//    }
//    
//    /**
//     * Removes the value of the attribute with the given type of the given entity or throws an {@link SQLException} if the attribute has a different value.
//     * 
//     * @param entity the entity of the attribute whose value is to be removed.
//     * @param type the type of the attribute whose value is to be removed.
//     * @param published whether to remove the published or unpublished value.
//     * @param value a block of type {@code attribute@virtualid.ch}.
//     */
//    public static void removeValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published, @Nonnull Block value) throws SQLException {
//        @Nonnull String statement = "DELETE FROM attribute_value WHERE entity = ? AND type = ? AND published = ? AND value = ?";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
//            preparedStatement.setLong(1, entity.getNumber());
//            preparedStatement.setLong(2, type.getNumber());
//            preparedStatement.setBoolean(3, published);
//            Database.setBlock(preparedStatement, 4, value);
//            if (preparedStatement.executeUpdate() == 0) {
//                if (type.hasBeenMerged()) removeValue(connection, entity, type, published, value);
//                else throw new SQLException("The value of the attribute with the type '" + type + "' of the entity '" + entity + "' could not be removed.");
//            }
//        }
//    }
//    
//    /**
//     * Replaces the value of the attribute with the given type of the given entity or throws an {@link SQLException} if it is not the old value of the attribute.
//     * 
//     * @param entity the entity of the attribute whose value is to be replaced.
//     * @param type the type of the attribute whose value is to be removed.
//     * @param published whether to remove the published or unpublished value.
//     * @param oldValue the old value to be replaced by the new value.
//     * @param newValue the new value by which the old value is replaced.
//     */
//    public static void replaceValue(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published, @Nonnull Block oldValue, @Nonnull Block newValue) throws SQLException {
//        @Nonnull String statement = "UPDATE attribute_value SET value = ? WHERE entity = ? AND type = ? AND published = ? AND value = ?";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
//            Database.setBlock(preparedStatement, 1, newValue);
//            preparedStatement.setLong(2, entity.getNumber());
//            preparedStatement.setLong(3, type.getNumber());
//            preparedStatement.setBoolean(4, published);
//            Database.setBlock(preparedStatement, 5, oldValue);
//            if (preparedStatement.executeUpdate() == 0) {
//                if (type.hasBeenMerged()) replaceValue(connection, entity, type, published, oldValue, newValue);
//                else throw new SQLException("The value of the attribute with the type '" + type + "' of the entity '" + entity + "' could not be replaced.");
//            }
//        }
//    }
//    
//    
//    /**
//     * Returns the visibility of the attribute with the given type of the given entity or null if no such visibility is available.
//     * 
//     * @param entity the entity of the attribute whose visibility is to be returned.
//     * @param type the type of the attribute whose visibility is to be returned.
//     * @return the visibility of the attribute with the given type of the given entity or null if no such visibility is available.
//     */
//    public static @Nullable PassiveExpression getVisibility(@Nonnull Entity entity, @Nonnull SemanticType type) throws SQLException {
//        @Nonnull String query = "SELECT visibility FROM attribute_visibility WHERE entity = " + entity + " AND type = " + type;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            if (resultSet.next()) return new PassiveExpression(resultSet.getString(1));
//            else return null;
//        }
//    }
//    
//    /**
//     * Adds the visibility of the attribute with the given type of the given entity or throws an {@link SQLException} if the visibility of the attribute is already set.
//     * 
//     * @param entity the entity of the attribute whose visibility is to be added.
//     * @param type the type of the attribute whose visibility is to be added.
//     * @param visibility the visibility of the attribute with the given type.
//     */
//    public static void addVisibility(@Nonnull Entity entity, @Nonnull SemanticType type, @Nonnull PassiveExpression visibility) throws SQLException {
//        @Nonnull String statement = "INSERT INTO attribute_visibility (entity, type, visibility) VALUES (?, ?, ?)";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
//            preparedStatement.setLong(1, entity.getNumber());
//            preparedStatement.setLong(2, type.getNumber());
//            preparedStatement.setString(3, visibility.toString());
//            preparedStatement.executeUpdate();
//        } catch (@Nonnull SQLException exception) {
//            if (type.hasBeenMerged()) addVisibility(connection, entity, type, visibility);
//            else throw exception;
//        }
//    }
//    
//    /**
//     * Removes the visibility of the attribute with the given type of the given entity or throws an {@link SQLException} if the attribute has a different visibility.
//     * 
//     * @param entity the entity of the attribute whose visibility is to be removed.
//     * @param type the type of the attribute whose visibility is to be removed.
//     * @param visibility the visibility of the attribute which is to be removed.
//     */
//    public static void removeVisibility(@Nonnull Entity entity, @Nonnull SemanticType type, @Nonnull PassiveExpression visibility) throws SQLException {
//        @Nonnull String statement = "DELETE FROM attribute_visibility WHERE entity = ? AND type = ? AND visibility = ?";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
//            preparedStatement.setLong(1, entity.getNumber());
//            preparedStatement.setLong(2, type.getNumber());
//            preparedStatement.setString(3, visibility.toString());
//            if (preparedStatement.executeUpdate() == 0) {
//                if (type.hasBeenMerged()) removeVisibility(connection, entity, type, visibility);
//                else throw new SQLException("The visibility of the attribute with the type '" + type + "' of the entity '" + entity + "' could not be removed.");
//            }
//        }
//    }
//    
//    /**
//     * Replaces the visibility of the attribute with the given type of the given entity or throws an {@link SQLException} if it is not the old visibility of the attribute.
//     * 
//     * @param entity the entity of the attribute whose visibility is to be replaced.
//     * @param type the type of the attribute whose visibility is to be removed.
//     * @param oldVisibility the old visibility to be replaced by the new visibility.
//     * @param newVisibility the new visibility by which the old visibility is replaced.
//     */
//    public static void replaceVisibility(@Nonnull Entity entity, @Nonnull SemanticType type, @Nonnull PassiveExpression oldVisibility, @Nonnull PassiveExpression newVisibility) throws SQLException {
//        @Nonnull String statement = "UPDATE attribute_visibility SET visibility = ? WHERE entity = ? AND type = ? AND visibility = ?";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
//            preparedStatement.setString(1, newVisibility.toString());
//            preparedStatement.setLong(2, entity.getNumber());
//            preparedStatement.setLong(3, type.getNumber());
//            preparedStatement.setString(4, oldVisibility.toString());
//            if (preparedStatement.executeUpdate() == 0) {
//                if (type.hasBeenMerged()) replaceVisibility(connection, entity, type, oldVisibility, newVisibility);
//                else throw new SQLException("The visibility of the attribute with the type '" + type + "' of the entity '" + entity + "' could not be replaced.");
//            }
//        }
//    }
    
}
