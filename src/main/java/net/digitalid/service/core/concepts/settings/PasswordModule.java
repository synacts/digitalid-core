package net.digitalid.service.core.concepts.settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.storage.Service;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.Account;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.EntityImplementation;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identifier.IdentifierImplementation;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.site.Site;

/**
 * This class provides database access to the {@link Settings passwords} of the core service.
 */
@Stateless
public final class PasswordModule implements StateModule {
    
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
    public void createTables(@Nonnull Site site) throws AbortException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "password (entity " + EntityImplementation.FORMAT + " NOT NULL, password VARCHAR(50) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", PRIMARY KEY (entity), FOREIGN KEY (entity) " + site.getEntityReference() + ")");
            Database.onInsertIgnore(statement, site + "password", "entity");
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws AbortException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotIgnore(statement, site + "password");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "password");
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.password.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.password.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, Settings.TYPE);
    
    /**
     * Stores the semantic type {@code password.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("password.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws AbortException {
        final @Nonnull String SQL = "SELECT entity, password FROM " + host + "password";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
            while (resultSet.next()) {
                final @Nonnull Account account = Account.getNotNull(host, resultSet, 1);
                final @Nonnull String password = resultSet.getString(2);
                entries.add(new TupleWrapper(MODULE_ENTRY, account.getIdentity().getAddress(), new StringWrapper(Settings.TYPE, password)).toBlock());
            }
            return new ListWrapper(MODULE_FORMAT, entries.freeze()).toBlock();
        }
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull String SQL = "INSERT INTO " + host + "password (entity, password) VALUES (?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(entry).getNonNullableElements(2);
                preparedStatement.setLong(1, IdentifierImplementation.create(elements.getNonNullable(0)).getIdentity().toInternalNonHostIdentity().getNumber());
                preparedStatement.setString(2, new StringWrapper(elements.getNonNullable(1)).getString());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    
    /**
     * Stores the semantic type {@code passwords.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.map("passwords.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Settings.TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws AbortException {
        return new TupleWrapper(STATE_FORMAT, restrictions.isClient() ? new StringWrapper(Settings.TYPE, get(entity)) : null).toBlock();
    }
    
    @Override
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws AbortException, InvalidEncodingException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nullable Block element = new TupleWrapper(block).getNullableElement(0);
        if (element != null) { set(entity, new StringWrapper(element).getString()); }
        
        Settings.reset(entity);
    }
    
    @Override
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws AbortException {
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
    static @Nonnull String get(@Nonnull Entity entity) throws AbortException {
        final @Nonnull String SQL = "SELECT password FROM " + entity.getSite() + "password WHERE entity = " + entity;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull String value = resultSet.getString(1);
                if (!Settings.isValid(value)) { throw new SQLException("The stored password is not valid."); }
                return value;
            } else { throw new SQLException(entity.getIdentity().getAddress() + " has no password."); }
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
    public static void set(@Nonnull Entity entity, @Nonnull String value) throws AbortException {
        assert Settings.isValid(value) : "The value is valid.";
        
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
     * @throws AbortException if the passed value is not the old value.
     * 
     * @require Password.isValid(oldValue) : "The old value is valid.";
     * @require Password.isValid(newValue) : "The new value is valid.";
     */
    @NonCommitting
    static void replace(@Nonnull Settings password, @Nonnull String oldValue, @Nonnull String newValue) throws AbortException {
        assert Settings.isValid(oldValue) : "The old value is valid.";
        assert Settings.isValid(newValue) : "The new value is valid.";
        
        final @Nonnull Entity entity = password.getEntity();
        final @Nonnull String SQL = "UPDATE " + entity.getSite() + "password SET password = ? WHERE entity = ? AND password = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            preparedStatement.setString(1, newValue);
            entity.set(preparedStatement, 2);
            preparedStatement.setString(3, oldValue);
            if (preparedStatement.executeUpdate() == 0) { throw new SQLException("The password of " + entity.getIdentity().getAddress() + " could not be replaced."); }
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
