package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concepts.Password;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.EntityClass;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.server.Host;
import ch.virtualid.service.CoreService;
import ch.virtualid.service.Service;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyArray;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
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
 * This class provides database access to the {@link Password passwords} of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Passwords implements BothModule {
    
    public static final Passwords MODULE = new Passwords();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "password (entity " + EntityClass.FORMAT + " NOT NULL, password VARCHAR(50) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", PRIMARY KEY (entity), FOREIGN KEY (entity) " + site.getEntityReference() + ")");
            Database.onInsertIgnore(statement, site + "password", "entity");
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotIgnore(statement, site + "password");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "password");
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.passwords.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.create("entry.passwords.module@virtualid.ch").load(TupleWrapper.TYPE, Identity.IDENTIFIER, Password.TYPE);
    
    /**
     * Stores the semantic type {@code passwords.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.create("passwords.module@virtualid.ch").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull String SQL = "SELECT entity, password FROM " + host + "password";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
            while (resultSet.next()) {
                final @Nonnull Account account = Account.getNotNull(host, resultSet, 1);
                final @Nonnull String password = resultSet.getString(2);
                entries.add(new TupleWrapper(MODULE_ENTRY, account.getIdentity().getAddress(), new StringWrapper(Password.TYPE, password)).toBlock());
            }
            return new ListWrapper(MODULE_FORMAT, entries.freeze()).toBlock();
        }
    }
    
    @Override
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull String SQL = "INSERT INTO " + host + "password (entity, password) VALUES (?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(SQL)) {
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(2);
                preparedStatement.setLong(1, IdentifierClass.create(elements.getNotNull(0)).getIdentity().toInternalNonHostIdentity().getNumber());
                preparedStatement.setString(2, new StringWrapper(elements.getNotNull(1)).getString());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    
    /**
     * Stores the semantic type {@code passwords.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.create("passwords.state@virtualid.ch").load(TupleWrapper.TYPE, Password.TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadonlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        return new TupleWrapper(STATE_FORMAT, restrictions.isClient() ? new StringWrapper(Password.TYPE, get(entity)) : null).toBlock();
    }
    
    @Override
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nullable Block element = new TupleWrapper(block).getElement(0);
        if (element != null) set(entity, new StringWrapper(element).getString());
        
        Password.reset(entity);
    }
    
    @Override
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
    public static @Nonnull String get(@Nonnull Entity entity) throws SQLException {
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
    public static void replace(@Nonnull Password password, @Nonnull String oldValue, @Nonnull String newValue) throws SQLException {
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
