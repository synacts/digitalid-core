package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concepts.Password;
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
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
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
 * @version 1.7
 */
public final class Passwords implements BothModule {
    
    static { CoreService.SERVICE.add(new Passwords()); }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "_password (entity BIGINT NOT NULL, password VARCHAR(50) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", PRIMARY KEY (entity), FOREIGN KEY (entity) REFERENCES " + site.getReference() + ")");
            Database.getConfiguration().onInsertUpdate(statement, site + "_password", 1, "entity", "password");
        }
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.passwords.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.create("entry.passwords.module@virtualid.ch").load(TupleWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code passwords.module@virtualid.ch}.
     */
    public static final @Nonnull SemanticType MODULE = SemanticType.create("passwords.module@virtualid.ch").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE;
    }
    
    @Pure
    @Override
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
        try (final @Nonnull Statement statement = Database.getConnection().createStatement()) {
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
     * Stores the semantic type {@code passwords.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.create("passwords.state@virtualid.ch").load(TupleWrapper.TYPE, Password.TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE;
    }
    
    @Pure
    @Override
    public @Nonnull Block getState(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(1);
        if (agent.isClient()) elements.set(0, new StringWrapper(Password.TYPE, get(entity)).toBlock());
        return new TupleWrapper(STATE, elements.freeze()).toBlock();
    }
    
    @Override
    public void addState(@Nonnull Entity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nullable Block element = new TupleWrapper(block).getElement(0);
        if (element != null) set(entity, new StringWrapper(element).getString());
    }
    
    @Override
    public void removeState(@Nonnull Entity entity) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("DELETE FROM " + entity.getSite() + "_password WHERE entity = " + entity);
        }
    }
    
    @Pure
    @Override
    public @Nullable InternalQuery getInternalQuery(@Nonnull Role role) {
        return null;
    }
    
    
    /**
     * Returns the password of the given entity.
     * 
     * @param entity the entity whose password is to be returned.
     * 
     * @return the password of the given entity.
     */
    public static @Nonnull String get(@Nonnull Entity entity) throws SQLException {
        final @Nonnull String SQL = "SELECT password FROM " + entity.getSite() + "_password WHERE entity = " + entity;
        try (@Nonnull Statement statement = Database.getConnection().createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) return resultSet.getString(1);
            else throw new SQLException(entity.getIdentity().getAddress().toString() + " has no password.");
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
        
        final @Nonnull String SQL = Database.getConfiguration().REPLACE() + " INTO " + entity.getSite() + "_password (entity, password) VALUES (?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.getConnection().prepareStatement(SQL)) {
            preparedStatement.setLong(1, entity.getNumber());
            preparedStatement.setString(2, value);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Replaces the value of the given password or throws an {@link SQLException} if the passed value is not the old value.
     * 
     * @param password the password whose value is to be replaced.
     * @param oldValue the old value to be replaced with the new value.
     * @param newValue the new value with which the old value is replaced.
     * 
     * @require Password.isValid(oldValue) : "The old value is valid.";
     * @require Password.isValid(newValue) : "The new value is valid.";
     */
    public static void replace(@Nonnull Password password, @Nonnull String oldValue, @Nonnull String newValue) throws SQLException {
        assert Password.isValid(oldValue) : "The old value is valid.";
        assert Password.isValid(newValue) : "The new value is valid.";
        
        final @Nonnull Entity entity = password.getEntityNotNull();
        final @Nonnull String SQL = "UPDATE " + entity.getSite() + "_password SET password = ? WHERE entity = ? AND password = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.getConnection().prepareStatement(SQL)) {
            preparedStatement.setString(1, newValue);
            preparedStatement.setLong(2, entity.getNumber());
            preparedStatement.setString(3, oldValue);
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The password of " + entity.getIdentity().getAddress() + " could not be replaced.");
        }
    }
    
}
