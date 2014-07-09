package ch.virtualid.module;

import ch.virtualid.authorization.Agent;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.database.Database;
import ch.virtualid.concept.Entity;
import ch.virtualid.database.Entity;
import ch.xdf.Block;
import ch.xdf.StringWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides database access to the passwords of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Passwords extends Module {
    
    /**
     * Initializes the database by creating the appropriate tables if necessary.
     * 
     * @param connection an open client or host connection to the database.
     */
    Passwords(@Nonnull Entity connection) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS password (entity BIGINT NOT NULL, password VARCHAR(50) NOT NULL COLLATE " + Database.UTF16_BIN + ", PRIMARY KEY (entity), FOREIGN KEY (entity) REFERENCES " + connection.getReference() + ")");
        }
    }
    
    /**
     * Returns the password of the given entity or null if not available.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose password is to be returned.
     * @return the password of the given entity or null if not available.
     */
    public static @Nullable String get(@Nonnull Entity connection, @Nonnull Entity entity) throws SQLException {
        @Nonnull String SQL = "SELECT password FROM password WHERE entity = " + entity;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            return resultSet.next() ? resultSet.getString(1) : null;
        }
    }
    
    /**
     * Sets the password of the given entity.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose password is to be set.
     * @param password the password to be set.
     * @require password.length() <= 50 : "The password may have at most 50 characters.";
     */
    public static void set(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull String password) throws SQLException {
        assert password.length() <= 50 : "The password may have at most 50 characters.";
        
        @Nonnull String SQL = "REPLACE INTO password (entity, password) VALUES (?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {
            preparedStatement.setLong(1, entity.getNumber());
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Removes the password from the given entity.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose password is to be removed.
     */
    public static void remove(@Nonnull Entity connection, @Nonnull Entity entity) throws SQLException {
        @Nonnull String SQL = "DELETE FROM password WHERE entity = " + entity;
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate(SQL);
        }
    }
    
    /**
     * Replaces the password of the given entity or throws an {@link SQLException} if it is not the old password of the given entity.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose password is to be replaced.
     * @param oldPassword the old password to be replaced by the new password.
     * @param newPassword the new password by which the old password is replaced.
     * @require oldPassword.length() <= 50 && newPassword.length() <= 50 : "The passwords may have at most 50 characters.";
     */
    public static void replace(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull String oldPassword, @Nonnull String newPassword) throws SQLException {
        assert oldPassword.length() <= 50 && newPassword.length() <= 50 : "The passwords may have at most 50 characters.";
        
        @Nonnull String SQL = "UPDATE password SET password = ? WHERE entity = ? AND password = ?";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {
            preparedStatement.setString(1, newPassword);
            preparedStatement.setLong(2, entity.getNumber());
            preparedStatement.setString(3, oldPassword);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The password of the entity '" + entity + "' could not be replaced.");
        }
    }
    
    
    /**
     * Returns the state of the given entity restricted by the authorization of the given agent.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose state is to be returned.
     * @param agent the agent whose authorization restricts the returned state.
     * @return the state of the given entity restricted by the authorization of the given agent.
     */
    @Override
    protected @Nonnull Block getAll(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        @Nullable Restrictions restrictions = agent.getRestrictions();
        if (restrictions != null && restrictions.isClient()) {
            @Nullable String password = get(connection, entity);
            if (password != null) return new StringWrapper(password).toBlock();
        }
        return Block.EMPTY;
    }
    
    /**
     * Adds the state in the given block to the given entity.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which the state is to be added.
     * @param block the block containing the state to be added.
     */
    @Override
    protected void addAll(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        set(connection, entity, new StringWrapper(block).getString());
    }
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose entries are to be removed.
     */
    @Override
    protected void removeAll(@Nonnull Entity connection, @Nonnull Entity entity) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM password WHERE entity = " + entity);
        }
    }
    
}
