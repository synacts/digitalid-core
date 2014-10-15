package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concepts.Attribute;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Site;
import ch.virtualid.expression.PassiveExpression;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.Module;
import ch.xdf.Block;
import ch.xdf.TupleWrapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides database access to the attributes of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Attributes extends BothModule {
    
    /**
     * Stores the semantic type {@code attributes.module@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("attributes.module@virtualid.ch").load(TupleWrapper.TYPE, );
    
    @Pure
    @Override
    public @Nonnull SemanticType getFormat() {
        return TYPE;
    }
    
    
    static { Module.add(new Attributes()); }
    
    @Override
    protected void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS attribute_value (entity BIGINT NOT NULL, type BIGINT NOT NULL, published BOOLEAN NOT NULL, value LONGBLOB NOT NULL, PRIMARY KEY (entity, type, published), FOREIGN KEY (entity) REFERENCES " + connection.getReference() + ", FOREIGN KEY (type) REFERENCES map_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS attribute_visibility (entity BIGINT NOT NULL, type BIGINT NOT NULL, visibility TEXT NOT NULL COLLATE " + Database.UTF16_BIN + ", PRIMARY KEY (entity, type), FOREIGN KEY (entity) REFERENCES " + connection.getReference() + ", FOREIGN KEY (type) REFERENCES map_identity (identity))");
        }
    }
    
    
    /**
     * Returns the value of the given attribute or null if no such value is available.
     * 
     * @param attribute the attribute whose value is to be returned.
     * @param published whether the attribute is already published.
     * @return the value (a block of type {@code attribute@virtualid.ch}) of the given attribute or null if no such value is available.
     */
    public static @Nullable Block getValue(@Nonnull Attribute attribute, boolean published) throws SQLException {
        @Nonnull String query = "SELECT value FROM attribute_value WHERE entity = " + attribute.getEntityNotNull() + " AND type = " + attribute.getType() + " AND published = " + published;
        try (@Nonnull Statement statement = attribute.getConnection().createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) return new Block(resultSet.getBytes(1));
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
    
    
//        statement.executeUpdate("CREATE TABLE IF NOT EXISTS attribute_value (entity BIGINT NOT NULL, type BIGINT NOT NULL, published BOOLEAN NOT NULL, value LONGBLOB NOT NULL, PRIMARY KEY (entity, type, published), FOREIGN KEY (entity) REFERENCES " + reference + ", FOREIGN KEY (type) REFERENCES map_identity (identity))");
//        statement.executeUpdate("CREATE TABLE IF NOT EXISTS attribute_visibility (entity BIGINT NOT NULL, type BIGINT NOT NULL, visibility TEXT NOT NULL COLLATE " + Database.UTF16_BIN + ", PRIMARY KEY (entity, type), FOREIGN KEY (entity) REFERENCES " + reference + ", FOREIGN KEY (type) REFERENCES map_identity (identity))");
    
    /**
     * Returns the value of the attribute with the given type of the given entity or null if no such value is available.
     * 
     * @param entity the entity of the attribute whose value is to be returned.
     * @param type the type of the attribute whose value is to be returned.
     * @param published whether the attribute is already published.
     * @return the value (a block of type {@code attribute@virtualid.ch}) of the attribute with the given type of the given entity or null if no such attribute is available.
     */
    public static @Nonnull Block getAll(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        @Nonnull List<Block> values = new LinkedList<Block>();
        // TODO: Rather Agent instead of permissions?
        
        @Nonnull String query = "SELECT type, published, value FROM attribute_value WHERE entity = " + entity + " AND type = " + type + " AND published = " + published;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) return new Block(resultSet.getBytes(1));
            else return null;
        }
    }
    
    /**
     * Returns the value of the attribute with the given type of the given entity or null if no such value is available.
     * 
     * @param entity the entity of the attribute whose value is to be returned.
     * @param type the type of the attribute whose value is to be returned.
     * @param published whether the attribute is already published.
     * @return the value (a block of type {@code attribute@virtualid.ch}) of the attribute with the given type of the given entity or null if no such attribute is available.
     */
    public static void addAll(@Nonnull Entity entity, @Nonnull SemanticType type, boolean published) throws SQLException {
        @Nonnull String query = "SELECT value FROM attribute_value WHERE entity = " + entity + " AND type = " + type + " AND published = " + published;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) return new Block(resultSet.getBytes(1));
            else return null;
        }
    }
    
    /**
     * Removes all the attributes of the given entity (both their values and visibilities).
     * 
     * @param entity the entity whose attributes are to be removed.
     */
    public static void removeAll(@Nonnull Entity entity) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM attribute_value WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM attribute_visibility WHERE entity = " + entity);
        }
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
