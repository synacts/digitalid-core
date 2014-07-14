package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Site;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.Module;
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
 * @version 1.8
 */
public final class Passwords extends BothModule {
    
    /**
     * Stores the semantic type {@code passwords.module@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("passwords.module@virtualid.ch").load(StringWrapper.TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    static { Module.add(new Passwords()); }
    
    @Override
    protected void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "_password (entity BIGINT NOT NULL, password VARCHAR(50) NOT NULL COLLATE " + Database.UTF16_BIN + ", PRIMARY KEY (entity), FOREIGN KEY (entity) REFERENCES " + connection.getReference() + ")");
        }
    }
    
    
    /**
     * Returns the password of the given entity or null if not available.
     * 
     * @param entity the entity whose password is to be returned.
     * @return the password of the given entity or null if not available.
     */
    public static @Nullable String get(@Nonnull Entity entity) throws SQLException {
        @Nonnull String SQL = "SELECT password FROM password WHERE entity = " + entity;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            return resultSet.next() ? resultSet.getString(1) : null;
        }
    }
    
    /**
     * Sets the password of the given entity.
     * 
     * @param entity the entity whose password is to be set.
     * @param password the password to be set.
     * @require password.length() <= 50 : "The password may have at most 50 characters.";
     */
    public static void set(@Nonnull Entity entity, @Nonnull String password) throws SQLException {
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
     * @param entity the entity whose password is to be removed.
     */
    public static void remove(@Nonnull Entity entity) throws SQLException {
        @Nonnull String SQL = "DELETE FROM password WHERE entity = " + entity;
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate(SQL);
        }
    }
    
    /**
     * Replaces the password of the given entity or throws an {@link SQLException} if it is not the old password of the given entity.
     * 
     * @param entity the entity whose password is to be replaced.
     * @param oldPassword the old password to be replaced by the new password.
     * @param newPassword the new password by which the old password is replaced.
     * @require oldPassword.length() <= 50 && newPassword.length() <= 50 : "The passwords may have at most 50 characters.";
     */
    public static void replace(@Nonnull Entity entity, @Nonnull String oldPassword, @Nonnull String newPassword) throws SQLException {
        assert oldPassword.length() <= 50 && newPassword.length() <= 50 : "The passwords may have at most 50 characters.";
        
        @Nonnull String SQL = "UPDATE password SET password = ? WHERE entity = ? AND password = ?";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {
            preparedStatement.setString(1, newPassword);
            preparedStatement.setLong(2, entity.getNumber());
            preparedStatement.setString(3, oldPassword);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The password of the entity '" + entity + "' could not be replaced.");
        }
    }
    
    
    @Pure
    @Override
    protected @Nonnull Block getAll(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        @Nullable Restrictions restrictions = agent.getRestrictions();
        if (restrictions != null && restrictions.isClient()) {
            @Nullable String password = get(entity);
            if (password != null) return new StringWrapper(TYPE, password).toBlock();
        }
        return null;
    }
    
    @Override
    protected void addAll(@Nonnull Entity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        set(entity, new StringWrapper(block).getString());
    }
    
    @Override
    protected void removeAll(@Nonnull Entity entity) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("DELETE FROM password WHERE entity = " + entity);
        }
    }
    
}
