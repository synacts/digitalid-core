/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// TODO: Remove this class once all of this works.

//package net.digitalid.core.settings;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.digitalid.utility.annotations.method.Pure;
//import net.digitalid.utility.collections.freezable.FreezableList;
//import net.digitalid.utility.collections.list.FreezableLinkedList;
//import net.digitalid.utility.collections.list.ReadOnlyList;
//import net.digitalid.utility.collections.readonly.ReadOnlyArray;
//import net.digitalid.utility.contracts.Require;
//import net.digitalid.utility.exceptions.external.InvalidEncodingException;
//import net.digitalid.utility.exceptions.ExternalException;
//import net.digitalid.utility.validation.annotations.type.Stateless;
//
//import net.digitalid.database.annotations.transaction.NonCommitting;
//import net.digitalid.database.core.exceptions.DatabaseException;
//import net.digitalid.database.core.table.Site;
//import net.digitalid.database.interfaces.Database;
//
//import net.digitalid.core.agent.Agent;
//import net.digitalid.core.agent.ReadOnlyAgentPermissions;
//import net.digitalid.core.agent.Restrictions;
//import net.digitalid.core.conversion.Block;
//import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
//import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
//import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
//import net.digitalid.core.service.CoreService;
//
//import net.digitalid.service.core.dataservice.StateModule;
//import net.digitalid.service.core.entity.Account;
//import net.digitalid.service.core.entity.Entity;
//import net.digitalid.service.core.entity.EntityImplementation;
//import net.digitalid.service.core.entity.NonHostEntity;
//import net.digitalid.service.core.identifier.IdentifierImplementation;
//import net.digitalid.service.core.identity.Identity;
//import net.digitalid.service.core.identity.SemanticType;
//import net.digitalid.service.core.site.host.Host;
//import net.digitalid.service.core.storage.Service;
//
///**
// * This class provides database access to the {@link Settings passwords} of the core service.
// */
//@Stateless
//public final class PasswordModule implements StateModule {
//    
//    /**
//     * Stores an instance of this module.
//     */
//    public static final PasswordModule MODULE = new PasswordModule();
//    
//    @Pure
//    @Override
//    public @Nonnull Service getService() {
//        return CoreService.SERVICE;
//    }
//    
//    @Override
//    @NonCommitting
//    public void createTables(@Nonnull Site site) throws DatabaseException {
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "password (entity " + EntityImplementation.FORMAT + " NOT NULL, password VARCHAR(50) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", PRIMARY KEY (entity), FOREIGN KEY (entity) " + site.getEntityReference() + ")");
//            Database.onInsertIgnore(statement, site + "password", "entity");
//        }
//    }
//    
//    @Override
//    @NonCommitting
//    public void deleteTables(@Nonnull Site site) throws DatabaseException {
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            Database.onInsertNotIgnore(statement, site + "password");
//            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "password");
//        }
//    }
//    
//    
//    /**
//     * Stores the semantic type {@code entry.password.module@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.password.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, Settings.TYPE);
//    
//    /**
//     * Stores the semantic type {@code password.module@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("password.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, MODULE_ENTRY);
//    
//    @Pure
//    @Override
//    public @Nonnull SemanticType getModuleFormat() {
//        return MODULE_FORMAT;
//    }
//    
//    @Pure
//    @Override
//    @NonCommitting
//    public @Nonnull Block exportModule(@Nonnull Host host) throws DatabaseException {
//        final @Nonnull String SQL = "SELECT entity, password FROM " + host + "password";
//        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
//            final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
//            while (resultSet.next()) {
//                final @Nonnull Account account = Account.getNotNull(host, resultSet, 1);
//                final @Nonnull String password = resultSet.getString(2);
//                entries.add(TupleWrapper.encode(MODULE_ENTRY, account.getIdentity().getAddress(), StringWrapper.encodeNonNullable(Settings.TYPE, password)));
//            }
//            return ListWrapper.encode(MODULE_FORMAT, entries.freeze());
//        }
//    }
//    
//    @Override
//    @NonCommitting
//    public void importModule(@Nonnull Host host, @Nonnull Block block) throws ExternalException {
//        Require.that(block.getType().isBasedOn(getModuleFormat())).orThrow("The block is based on the format of this module.");
//        
//        final @Nonnull String SQL = "INSERT INTO " + host + "password (entity, password) VALUES (?, ?)";
//        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
//            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
//            for (final @Nonnull Block entry : entries) {
//                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(2);
//                preparedStatement.setLong(1, IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(0)).getIdentity().castTo(InternalNonHostIdentity.class).getNumber());
//                preparedStatement.setString(2, StringWrapper.decodeNonNullable(elements.getNonNullable(1)));
//                preparedStatement.addBatch();
//            }
//            preparedStatement.executeBatch();
//        }
//    }
//    
//    
//    /**
//     * Stores the semantic type {@code passwords.state@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.map("passwords.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Settings.TYPE);
//    
//    @Pure
//    @Override
//    public @Nonnull SemanticType getStateFormat() {
//        return STATE_FORMAT;
//    }
//    
//    @Pure
//    @Override
//    @NonCommitting
//    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException {
//        return TupleWrapper.encode(STATE_FORMAT, restrictions.isClient() ? StringWrapper.encodeNonNullable(Settings.TYPE, get(entity)) : null);
//    }
//    
//    @Override
//    @NonCommitting
//    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws DatabaseException, InvalidEncodingException {
//        Require.that(block.getType().isBasedOn(getStateFormat())).orThrow("The block is based on the indicated type.");
//        
//        final @Nullable Block element = TupleWrapper.decode(block).getNullableElement(0);
//        if (element != null) { set(entity, StringWrapper.decodeNonNullable(element)); }
//        
//        Settings.reset(entity);
//    }
//    
//    @Override
//    @NonCommitting
//    public void removeState(@Nonnull NonHostEntity entity) throws DatabaseException {
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("DELETE FROM " + entity.getSite() + "password WHERE entity = " + entity);
//        }
//    }
//    
//    
//    /**
//     * Returns the password of the given entity.
//     * 
//     * @param entity the entity whose password is to be returned.
//     * 
//     * @return the password of the given entity.
//     * 
//     * @ensure Password.isValid(return) : "The returned value is valid.";
//     */
//    @NonCommitting
//    static @Nonnull String get(@Nonnull Entity entity) throws DatabaseException {
//        final @Nonnull String SQL = "SELECT password FROM " + entity.getSite() + "password WHERE entity = " + entity;
//        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
//            if (resultSet.next()) {
//                final @Nonnull String value = resultSet.getString(1);
//                if (!Settings.isValid(value)) { throw new SQLException("The stored password is not valid."); }
//                return value;
//            } else { throw new SQLException(entity.getIdentity().getAddress() + " has no password."); }
//        }
//    }
//    
//    /**
//     * Sets the password of the given entity.
//     * 
//     * @param entity the entity whose password is to be set.
//     * @param value the value to set the password to.
//     * 
//     * @require Password.isValid(value) : "The value is valid.";
//     */
//    @NonCommitting
//    public static void set(@Nonnull Entity entity, @Nonnull String value) throws DatabaseException {
//        Require.that(Settings.isValid(value)).orThrow("The value is valid.");
//        
//        final @Nonnull String SQL = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + entity.getSite() + "password (entity, password) VALUES (?, ?)";
//        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
//            entity.set(preparedStatement, 1);
//            preparedStatement.setString(2, value);
//            preparedStatement.executeUpdate();
//        }
//    }
//    
//    /**
//     * Replaces the value of the given password.
//     * 
//     * @param password the password whose value is to be replaced.
//     * @param oldValue the old value to be replaced with the new value.
//     * @param newValue the new value with which the old value is replaced.
//     * 
//     * @throws DatabaseException if the passed value is not the old value.
//     * 
//     * @require Password.isValid(oldValue) : "The old value is valid.";
//     * @require Password.isValid(newValue) : "The new value is valid.";
//     */
//    @NonCommitting
//    static void replace(@Nonnull Settings password, @Nonnull String oldValue, @Nonnull String newValue) throws DatabaseException {
//        Require.that(Settings.isValid(oldValue)).orThrow("The old value is valid.");
//        Require.that(Settings.isValid(newValue)).orThrow("The new value is valid.");
//        
//        final @Nonnull Entity entity = password.getEntity();
//        final @Nonnull String SQL = "UPDATE " + entity.getSite() + "password SET password = ? WHERE entity = ? AND password = ?";
//        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
//            preparedStatement.setString(1, newValue);
//            entity.set(preparedStatement, 2);
//            preparedStatement.setString(3, oldValue);
//            if (preparedStatement.executeUpdate() == 0) { throw new SQLException("The password of " + entity.getIdentity().getAddress() + " could not be replaced."); }
//        }
//    }
//    
//    static { CoreService.SERVICE.add(MODULE); }
//    
//}
