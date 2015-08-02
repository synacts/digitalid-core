package net.digitalid.core.password;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Stateless;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Account;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.EntityClass;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.StringWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class provides database access to the {@link Password passwords} of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Stateless
public final class PasswordModule implements BothModule {
    
    /**
     * Stores an instance of this module.
     */
    public static final PasswordModule MODULE = new PasswordModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "password (entity " + EntityClass.FORMAT + " NOT NULL, password VARCHAR(50) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", PRIMARY KEY (entity), FOREIGN KEY (entity) " + site.getEntityReference() + ")");
            Database.onInsertIgnore(statement, site + "password", "entity");
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotIgnore(statement, site + "password");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "password");
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.password.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.password.module@core.digitalid.net").load(TupleWrapper.TYPE, Identity.IDENTIFIER, Password.TYPE);
    
    /**
     * Stores the semantic type {@code password.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("password.module@core.digitalid.net").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
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
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull String SQL = "INSERT INTO " + host + "password (entity, password) VALUES (?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(2);
                preparedStatement.setLong(1, IdentifierClass.create(elements.getNonNullable(0)).getIdentity().toInternalNonHostIdentity().getNumber());
                preparedStatement.setString(2, new StringWrapper(elements.getNonNullable(1)).getString());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    
    /**
     * Stores the semantic type {@code passwords.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.map("passwords.state@core.digitalid.net").load(TupleWrapper.TYPE, Password.TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        return new TupleWrapper(STATE_FORMAT, restrictions.isClient() ? new StringWrapper(Password.TYPE, get(entity)) : null).toBlock();
    }
    
    @Override
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nullable Block element = new TupleWrapper(block).getElement(0);
        if (element != null) set(entity, new StringWrapper(element).getString());
        
        Password.reset(entity);
    }
    
    @Override
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + entity.getSite() + "password WHERE entity = " + entity);
        }
    }
    
    
    /**
     * Returns the password of the given entity.
     * 
     * @param entity the entity whose password is to be returned.
     * 
     * @return the password of the given entity.
     * 
     * @ensure Password.isValid(return) : "The returned value is valid.";
     */
    @NonCommitting
    static @Nonnull String get(@Nonnull Entity entity) throws SQLException {
        final @Nonnull String SQL = "SELECT password FROM " + entity.getSite() + "password WHERE entity = " + entity;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull String value = resultSet.getString(1);
                if (!Password.isValid(value)) throw new SQLException("The stored password is not valid.");
                return value;
            } else throw new SQLException(entity.getIdentity().getAddress() + " has no password.");
        }
    }
    
    /**
     * Sets the password of the given entity.
     * 
     * @param entity the entity whose password is to be set.
     * @param value the value to set the password to.
     * 
     * @require Password.isValid(value) : "The value is valid.";
     */
    @NonCommitting
    public static void set(@Nonnull Entity entity, @Nonnull String value) throws SQLException {
        assert Password.isValid(value) : "The value is valid.";
        
        final @Nonnull String SQL = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + entity.getSite() + "password (entity, password) VALUES (?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            entity.set(preparedStatement, 1);
            preparedStatement.setString(2, value);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Replaces the value of the given password.
     * 
     * @param password the password whose value is to be replaced.
     * @param oldValue the old value to be replaced with the new value.
     * @param newValue the new value with which the old value is replaced.
     * 
     * @throws SQLException if the passed value is not the old value.
     * 
     * @require Password.isValid(oldValue) : "The old value is valid.";
     * @require Password.isValid(newValue) : "The new value is valid.";
     */
    @NonCommitting
    static void replace(@Nonnull Password password, @Nonnull String oldValue, @Nonnull String newValue) throws SQLException {
        assert Password.isValid(oldValue) : "The old value is valid.";
        assert Password.isValid(newValue) : "The new value is valid.";
        
        final @Nonnull Entity entity = password.getEntity();
        final @Nonnull String SQL = "UPDATE " + entity.getSite() + "password SET password = ? WHERE entity = ? AND password = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            preparedStatement.setString(1, newValue);
            entity.set(preparedStatement, 2);
            preparedStatement.setString(3, oldValue);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The password of " + entity.getIdentity().getAddress() + " could not be replaced.");
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
