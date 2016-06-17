package net.digitalid.core.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashSet;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.table.Site;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.conversion.wrappers.value.BooleanWrapper;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.EntityImplementation;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.expression.PassiveExpression;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.IdentityImplementation;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.resolution.Mapper;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.state.Service;

import net.digitalid.service.core.dataservice.StateModule;

/**
 * This class provides database access to the {@link Attribute attributes} of the core service.
 */
@Stateless
public final class AttributeModule implements StateModule {
    
    /* -------------------------------------------------- Module Initialization -------------------------------------------------- */
    
    /**
     * Stores an instance of this module.
     */
    public static final AttributeModule MODULE = new AttributeModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    /* -------------------------------------------------- Table Creation and Deletion -------------------------------------------------- */
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "attribute_value (entity " + EntityImplementation.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, published BOOLEAN NOT NULL, value " + AttributeValue.FORMAT + " NOT NULL, PRIMARY KEY (entity, type, published), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "attribute_visibility (entity " + EntityImplementation.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, visibility " + PassiveExpression.FORMAT + ", PRIMARY KEY (entity, type), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "attribute_visibility");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "attribute_value");
        }
    }
    
    /* -------------------------------------------------- Module Export and Import -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code entry.value.attribute.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VALUE_MODULE_ENTRY = SemanticType.map("entry.value.attribute.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, SemanticType.ATTRIBUTE_IDENTIFIER, Attribute.PUBLISHED, AttributeValue.TYPE);
    
    /**
     * Stores the semantic type {@code table.value.attribute.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VALUE_MODULE_TABLE = SemanticType.map("table.value.attribute.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, VALUE_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.visibility.attribute.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VISIBILITY_MODULE_ENTRY = SemanticType.map("entry.visibility.attribute.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, SemanticType.ATTRIBUTE_IDENTIFIER, PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code table.visibility.attribute.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VISIBILITY_MODULE_TABLE = SemanticType.map("table.visibility.attribute.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, VISIBILITY_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code attribute.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("attribute.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, VALUE_MODULE_TABLE, VISIBILITY_MODULE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws DatabaseException {
        final @Nonnull FreezableArray<Block> tables = FreezableArray.get(2);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, type, published, value FROM " + host + "attribute_value")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, 1);
                    final @Nonnull Identity type = IdentityImplementation.getNotNull(resultSet, 2);
                    final boolean published = resultSet.getBoolean(3);
                    final @Nonnull Block value = Block.getNotNull(AttributeValue.TYPE, resultSet, 4);
                    entries.add(TupleWrapper.encode(VALUE_MODULE_ENTRY, identity, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), BooleanWrapper.encode(Attribute.PUBLISHED, published), value.toBlockable()));
                }
                tables.set(0, ListWrapper.encode(VALUE_MODULE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, type, visibility FROM " + host + "attribute_visibility")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, 1);
                    final @Nonnull Identity type = IdentityImplementation.getNotNull(resultSet, 2);
                    final @Nonnull String visibility = resultSet.getString(3);
                    entries.add(TupleWrapper.encode(VISIBILITY_MODULE_ENTRY, identity, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), StringWrapper.encodeNonNullable(PassiveExpression.TYPE, visibility)));
                }
                tables.set(1, ListWrapper.encode(VISIBILITY_MODULE_TABLE, entries.freeze()));
            }
            
        }
        return TupleWrapper.encode(MODULE_FORMAT, tables.freeze());
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        Require.that(block.getType().isBasedOn(getModuleFormat())).orThrow("The block is based on the format of this module.");
        
        final @Nonnull ReadOnlyArray<Block> tables = TupleWrapper.decode(block).getNonNullableElements(2);
        final @Nonnull String prefix = "INSERT INTO " + host;
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "attribute_value (entity, type, published, value) VALUES (?, ?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(0));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(4);
                IdentityImplementation.create(elements.getNonNullable(0)).castTo(InternalIdentity.class).set(preparedStatement, 1);
                IdentityImplementation.create(elements.getNonNullable(1)).castTo(SemanticType.class).checkIsAttributeType().set(preparedStatement, 2);
                preparedStatement.setBoolean(3, BooleanWrapper.decode(elements.getNonNullable(2)));
                elements.getNonNullable(3).set(preparedStatement, 4);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "attribute_visibility (entity, type, visibility) VALUES (?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(1));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                IdentityImplementation.create(elements.getNonNullable(0)).castTo(InternalIdentity.class).set(preparedStatement, 1);
                IdentityImplementation.create(elements.getNonNullable(1)).castTo(SemanticType.class).checkIsAttributeType().set(preparedStatement, 2);
                preparedStatement.setString(2, StringWrapper.decodeNonNullable(elements.getNonNullable(2)));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    /* -------------------------------------------------- State Getter and Setter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code entry.value.attribute.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VALUE_STATE_ENTRY = SemanticType.map("entry.value.attribute.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, SemanticType.ATTRIBUTE_IDENTIFIER, Attribute.PUBLISHED, AttributeValue.TYPE);
    
    /**
     * Stores the semantic type {@code table.value.attribute.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VALUE_STATE_TABLE = SemanticType.map("table.value.attribute.state@core.digitalid.net").load(ListWrapper.XDF_TYPE, VALUE_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.visibility.attribute.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VISIBILITY_STATE_ENTRY = SemanticType.map("entry.visibility.attribute.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, SemanticType.ATTRIBUTE_IDENTIFIER, PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code table.visibility.attribute.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType VISIBILITY_STATE_TABLE = SemanticType.map("table.visibility.attribute.state@core.digitalid.net").load(ListWrapper.XDF_TYPE, VISIBILITY_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code attribute.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.map("attribute.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, VALUE_STATE_TABLE, VISIBILITY_STATE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException {
        final @Nonnull Site site = entity.getSite();
        final @Nonnull FreezableArray<Block> tables = FreezableArray.get(2);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT type, published, value FROM " + site + "attribute_value WHERE entity = " + entity + permissions.allTypesToString())) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity type = IdentityImplementation.getNotNull(resultSet, 1);
                    final boolean published = resultSet.getBoolean(2);
                    final @Nonnull Block value = Block.getNotNull(AttributeValue.TYPE, resultSet, 3);
                    entries.add(TupleWrapper.encode(VALUE_STATE_ENTRY, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), BooleanWrapper.encode(Attribute.PUBLISHED, published), value.toBlockable()));
                }
                tables.set(0, ListWrapper.encode(VALUE_STATE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT type, visibility FROM " + site + "attribute_visibility WHERE entity = " + entity + permissions.writeTypesToString())) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity type = IdentityImplementation.getNotNull(resultSet, 1);
                    final @Nonnull String visibility = resultSet.getString(2);
                    entries.add(TupleWrapper.encode(VISIBILITY_STATE_ENTRY, type.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), StringWrapper.encodeNonNullable(PassiveExpression.TYPE, visibility)));
                }
                tables.set(1, ListWrapper.encode(VISIBILITY_STATE_TABLE, entries.freeze()));
            }
            
        }
        return TupleWrapper.encode(STATE_FORMAT, tables.freeze());
    }
    
    @Override
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        Require.that(block.getType().isBasedOn(getStateFormat())).orThrow("The block is based on the indicated type.");
        
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertIgnore(statement, site + "attribute_value", "entity", "type", "published");
            Database.onInsertIgnore(statement, site + "attribute_visibility", "entity", "type");
        }
        
        final @Nonnull ReadOnlyArray<Block> tables = TupleWrapper.decode(block).getNonNullableElements(2);
        final @Nonnull String prefix = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + site;
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "attribute_value (entity, type, published, value) VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(0));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                IdentityImplementation.create(elements.getNonNullable(0)).castTo(SemanticType.class).checkIsAttributeFor(entity).set(preparedStatement, 2);
                preparedStatement.setBoolean(3, BooleanWrapper.decode(elements.getNonNullable(1)));
                elements.getNonNullable(2).set(preparedStatement, 4);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "attribute_visibility (entity, type, visibility) VALUES (?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(1));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(2);
                IdentityImplementation.create(elements.getNonNullable(0)).castTo(SemanticType.class).checkIsAttributeFor(entity).set(preparedStatement, 2);
                preparedStatement.setString(3, StringWrapper.decodeNonNullable(elements.getNonNullable(1)));
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
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws DatabaseException {
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + site + "attribute_visibility WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "attribute_value WHERE entity = " + entity);
        }
    }
    
    /* -------------------------------------------------- Attribute Retrieval -------------------------------------------------- */
    
    /**
     * Returns all the attributes of the given entity.
     * 
     * @param entity the entity whose attributes are to be returned.
     * 
     * @return all the attributes of the given entity.
     * 
     * @ensure return.!isFrozen() : "The returned attributes are not frozen.";
     */
    @Pure
    @NonCommitting
    static @Capturable @Nonnull FreezableSet<Attribute> getAll(@Nonnull Entity entity) throws DatabaseException {
        final @Nonnull String SQL = "SELECT DISTINCT type FROM " + entity.getSite() + "attribute_value WHERE entity = " + entity;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableSet<Attribute> attributes = new FreezableLinkedHashSet<>();
            while (resultSet.next()) { attributes.add(Attribute.get(entity, IdentityImplementation.getNotNull(resultSet, 1).castTo(SemanticType.class).checkIsAttributeFor(entity))); }
            return attributes;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /* -------------------------------------------------- Attribute Value -------------------------------------------------- */
    
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
    @NonCommitting
    static @Nullable AttributeValue getValue(@Nonnull Attribute attribute, boolean published) throws DatabaseException {
        final @Nonnull String SQL = "SELECT value FROM " + attribute.getEntity().getSite() + "attribute_value WHERE entity = " + attribute.getEntity() + " AND type = " + attribute.getType() + " AND published = " + Database.toBoolean(published);
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return AttributeValue.get(resultSet, 1).checkMatches(attribute); }
            else { return null; }
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
    @NonCommitting
    static void insertValue(@Nonnull Attribute attribute, boolean published, @Nonnull AttributeValue value) throws DatabaseException {
        Require.that(value.isVerified() && value.matches(attribute)).orThrow("The value is verified and matches the given attribute.");
        
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
    static void deleteValue(@Nonnull Attribute attribute, boolean published, @Nonnull AttributeValue value) throws DatabaseException {
        Require.that(value.isVerified() && value.matches(attribute)).orThrow("The value is verified and matches the given attribute.");
        
        final @Nonnull String SQL = "DELETE FROM " + attribute.getEntity().getSite() + "attribute_value WHERE entity = ? AND type = ? AND published = ? AND value = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            attribute.getEntity().set(preparedStatement, 1);
            attribute.getType().set(preparedStatement, 2);
            preparedStatement.setBoolean(3, published);
            value.set(preparedStatement, 4);
            if (preparedStatement.executeUpdate() == 0) { throw new SQLException("The value of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be deleted."); }
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
    @NonCommitting
    static void replaceValue(@Nonnull Attribute attribute, boolean published, @Nonnull AttributeValue oldValue, @Nonnull AttributeValue newValue) throws DatabaseException {
        Require.that(oldValue.isVerified() && oldValue.matches(attribute)).orThrow("The old value is verified and matches the given attribute.");
        Require.that(newValue.isVerified() && newValue.matches(attribute)).orThrow("The new value is verified and matches the given attribute.");
        
        final @Nonnull String SQL = "UPDATE " + attribute.getEntity().getSite() + "attribute_value SET value = ? WHERE entity = ? AND type = ? AND published = ? AND value = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            newValue.set(preparedStatement, 1);
            attribute.getEntity().set(preparedStatement, 2);
            attribute.getType().set(preparedStatement, 3);
            preparedStatement.setBoolean(4, published);
            oldValue.set(preparedStatement, 5);
            if (preparedStatement.executeUpdate() == 0) { throw new SQLException("The value of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be replaced."); }
        }
    }
    
    /* -------------------------------------------------- Attribute Visibility -------------------------------------------------- */
    
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
    @NonCommitting
    static @Nullable PassiveExpression getVisibility(@Nonnull Attribute attribute) throws DatabaseException {
        Require.that(attribute.getEntity().getIdentity() instanceof InternalPerson).orThrow("The entity of the given attribute belongs to an internal person.");
        
        final @Nonnull String SQL = "SELECT visibility FROM " + attribute.getEntity().getSite() + "attribute_visibility WHERE entity = " + attribute.getEntity() + " AND type = " + attribute.getType();
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return PassiveExpression.get((NonHostEntity) attribute.getEntity(), resultSet, 1); }
            else { return null; }
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
    @NonCommitting
    static void insertVisibility(@Nonnull Attribute attribute, @Nonnull PassiveExpression visibility) throws DatabaseException {
        Require.that(attribute.getEntity().getIdentity() instanceof InternalPerson).orThrow("The entity of the given attribute belongs to an internal person.");
        Require.that(visibility.getEntity().equals(attribute.getEntity())).orThrow("The visibility and the attribute belong to the same entity.");
        
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
    @NonCommitting
    static void deleteVisibility(@Nonnull Attribute attribute, @Nonnull PassiveExpression visibility) throws DatabaseException {
        Require.that(attribute.getEntity().getIdentity() instanceof InternalPerson).orThrow("The entity of the given attribute belongs to an internal person.");
        Require.that(visibility.getEntity().equals(attribute.getEntity())).orThrow("The visibility and the attribute belong to the same entity.");
        
        final @Nonnull String SQL = "DELETE FROM " + attribute.getEntity().getSite() + "attribute_visibility WHERE entity = ? AND type = ? AND visibility = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            attribute.getEntity().set(preparedStatement, 1);
            attribute.getType().set(preparedStatement, 2);
            visibility.set(preparedStatement, 3);
            if (preparedStatement.executeUpdate() == 0) { throw new SQLException("The visibility of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be deleted."); }
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
    @NonCommitting
    static void replaceVisibility(@Nonnull Attribute attribute, @Nonnull PassiveExpression oldVisibility, @Nonnull PassiveExpression newVisibility) throws DatabaseException {
        Require.that(attribute.getEntity().getIdentity() instanceof InternalPerson).orThrow("The entity of the given attribute belongs to an internal person.");
        Require.that(oldVisibility.getEntity().equals(attribute.getEntity())).orThrow("The old visibility and the attribute belong to the same entity.");
        Require.that(newVisibility.getEntity().equals(attribute.getEntity())).orThrow("The new visibility and the attribute belong to the same entity.");
        
        final @Nonnull String SQL = "UPDATE " + attribute.getEntity().getSite() + "attribute_visibility SET visibility = ? WHERE entity = ? AND type = ? AND visibility = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            newVisibility.set(preparedStatement, 1);
            attribute.getEntity().set(preparedStatement, 2);
            attribute.getType().set(preparedStatement, 3);
            oldVisibility.set(preparedStatement, 4);
            if (preparedStatement.executeUpdate() == 0) { throw new SQLException("The visibility of the attribute with the type " + attribute.getType().getAddress() + " of the entity " + attribute.getEntity().getIdentity().getAddress() + " could not be replaced."); }
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
