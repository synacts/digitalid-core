package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.Context;
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
 * This class provides database access to the {@link Context contexts} of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Contexts implements BothModule {
    
    static { CoreService.SERVICE.add(new Contexts()); }
    
    @Override
    public void createTables(@Nonnull Site site) throws SQLException {
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS context_name (identity BIGINT NOT NULL, context BIGINT NOT NULL, name VARCHAR(50) NOT NULL COLLATE " + Database.UTF16_BIN + ", PRIMARY KEY (identity, context), FOREIGN KEY (identity) REFERENCES general_identity (identity))");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS context_permission (identity BIGINT NOT NULL, context BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, context, type), FOREIGN KEY (identity) REFERENCES general_identity (identity), FOREIGN KEY (type) REFERENCES general_identity (identity))");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS context_authentication (identity BIGINT NOT NULL, context BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, context, type), FOREIGN KEY (identity) REFERENCES general_identity (identity), FOREIGN KEY (type) REFERENCES general_identity (identity))");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS context_subcontext (identity BIGINT NOT NULL, context BIGINT NOT NULL, subcontext BIGINT NOT NULL, sequence " + Database.getConfiguration().TINYINT() + ", PRIMARY KEY (identity, context, subcontext), FOREIGN KEY (identity) REFERENCES general_identity (identity))");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS context_contact (identity BIGINT NOT NULL, context BIGINT NOT NULL, contact BIGINT NOT NULL, PRIMARY KEY (identity, context, contact), FOREIGN KEY (identity) REFERENCES general_identity (identity), FOREIGN KEY (contact) REFERENCES general_identity (identity))");
//        }
//        
//        Mapper.addReference("context_contact", "contact");
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.contexts.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.create("entry.contexts.module@virtualid.ch").load(TupleWrapper.TYPE, ch.virtualid.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code contexts.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.create("contexts.module@virtualid.ch").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE;
    }
    
    @Pure
    @Override
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
        try (@Nonnull Statement statement = Database.createStatement()) {
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
     * Stores the semantic type {@code entry.contexts.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE_ENTRY = SemanticType.create("entry.contexts.state@virtualid.ch").load(TupleWrapper.TYPE, ch.virtualid.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code contexts.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE = SemanticType.create("contexts.state@virtualid.ch").load(ListWrapper.TYPE, STATE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE;
    }
    
    @Pure
    @Override
    public @Nonnull Block getState(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
        try (@Nonnull Statement statement = Database.createStatement()) {
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
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Remove the entries of the given entity from the database table(s).
        }
    }
    
    @Pure
    @Override
    public @Nullable InternalQuery getInternalQuery(@Nonnull Role role) {
        return null; // TODO: Return the internal query for reloading the data of this module.
    }
    
    
//    /**
//     * Returns whether the given context at the given identity exists.
//     * 
//     * @param identity the identity whose context is to be checked.
//     * @param context the context whose existence is to be checked.
//     * @return whether the given context at the given identity exists.
//     */
//    static boolean contextExists(@Nonnull NonHostIdentity identity, @Nonnull Context context) throws SQLException {
//        @Nonnull String query = "SELECT EXISTS(SELECT * FROM context_name WHERE identity = " + identity + " AND context = " + context + ")";
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            if (resultSet.next()) return resultSet.getBoolean(1);
//            else throw new SQLException("The executed statement should always have a result.");
//        }
//    }
//    
//    /**
//     * Returns the subcontexts of the given context at the given identity (including the given context).
//     * 
//     * @param identity the identity whose contexts are to be returned.
//     * @param context the supercontext of the requested contexts.
//     * @return the subcontexts of the given context at the given identity.
//     * @require contextExists(connection, identity, context) : "The given context has to exist.";
//     */
//    static @Nonnull Set<Pair<Context, String>> getSubcontexts(@Nonnull NonHostIdentity identity, @Nonnull Context context) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        @Nonnull String query = "SELECT context, name FROM context_name WHERE entity = " + identity + " AND context & " + context.getMask() + " = " + context;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            @Nonnull Set<Pair<Context, String>> contexts = new LinkedHashSet<Pair<Context, String>>();
//            while (resultSet.next()) contexts.add(new Pair<Context, String>(new Context(resultSet.getLong(1)), resultSet.getString(2)));
//            return contexts;
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("Some values returned by the database are invalid.", exception);
//        }
//    }
//    
//    /**
//     * Returns the name of the given context at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param context the context whose name is to be returned.
//     * @return the name of the given context at the given identity.
//     * @require contextExists(connection, identity, context) : "The given context has to exist.";
//     */
//    static @Nonnull String getContextName(@Nonnull NonHostIdentity identity, @Nonnull Context context) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        @Nonnull String query = "SELECT name FROM context_name WHERE identity = " + identity + " AND context = " + context;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            if (resultSet.next()) return resultSet.getString(2);
//            else throw new SQLException("The given context could not be found though it should exist.");
//        }
//    }
//    
//    /**
//     * Sets the name of the given context at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param context the context whose name is to be set.
//     * @param name the name to be set.
//     * @require contextExists(connection, identity, context.getSupercontext()) : "The supercontext of the given context has to exist.";
//     * @require name.length() <= 50 : "The context name may have at most 50 characters.";
//     */
//    static void setContextName(@Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull String name) throws SQLException {
//        assert contextExists(connection, identity, context.getSupercontext()) : "The supercontext of the given context has to exist.";
//        assert name.length() <= 50 : "The context name may have at most 50 characters.";
//        
//        @Nonnull String statement = "REPLACE INTO context_name (identity, context, name) VALUES (?, ?, ?)";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
//            preparedStatement.setLong(1, identity.getNumber());
//            preparedStatement.setLong(2, context.getNumber());
//            preparedStatement.setString(3, name);
//            preparedStatement.executeUpdate();
//        }
//    }
//    
//    /**
//     * Returns the element with the given position from the given set.
//     * 
//     * @param set the set whose element is to be returned.
//     * @param position the position of the element which is to be returned.
//     * @return the element with the given position from the given set.
//     * @require position >= 0 && position < set.size() : "The position is within the bounds of the set.";
//     */
//    public static <T> T getElement(@Nonnull Set<T> set, int position) {
//        assert position >= 0 && position < set.size() : "The position is within the bounds of the set.";
//        
//        int i = 0;
//        for (@Nonnull T element : set) {
//            if (i == position) return element;
//            i++;
//        }
//        
//        throw new ShouldNeverHappenError("The requested element of the set could not be found.");
//    }
//    
//    /**
//     * Returns the types from the given table with the given condition of the given identity.
//     * 
//     * @param identity the identity whose types are to be returned.
//     * @param table the name of the database table which is to be queried and which has to have a column with the name 'type'.
//     * @param condition a condition to filter the rows of the given database table.
//     * @return the types from the given table with the given condition of the given identity.
//     */
//    private static @Nonnull Set<SemanticType> getTypes(@Nonnull NonHostIdentity identity, @Nonnull String table, @Nonnull String condition) throws SQLException {
//        @Nonnull String query = "SELECT general_identity.identity, general_identity.category, general_identity.address FROM " + table + " JOIN general_identity ON " + table + ".type = general_identity.identity WHERE " + table + ".identity = " + identity + " AND " + table + "." + condition;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            @Nonnull Set<SemanticType> types = new LinkedHashSet<SemanticType>();
//            while (resultSet.next()) {
//                long number = resultSet.getLong(1);
//                @Nonnull Category category = Category.get(resultSet.getByte(2));
//                @Nonnull NonHostIdentifier address = new NonHostIdentifier(resultSet.getString(3));
//                types.add(Identity.create(category, number, address).toSemanticType());
//            }
//            return types;
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("Some values returned by the database are invalid.", exception);
//        }
//    }
//    
//    /**
//     * Adds the given types with the given value in the given column to the given table of the given identity.
//     * 
//     * @param identity the identity for which the types are to be added.
//     * @param table the name of the database table to which the types are to be added and which has to have columns with the names 'identity', 'type' and the given column name.
//     * @param column the name of the column which is to be filled with the given value.
//     * @param value the value to fill into the given column for every added type.
//     * @param types the types to be added to the given table of the given identity.
//     */
//    private static void addTypes(@Nonnull NonHostIdentity identity, @Nonnull String table, @Nonnull String column, long value, @Nonnull Set<SemanticType> types) throws SQLException {
//        @Nonnull String statement = "INSERT " + Database.IGNORE + " INTO " + table + " (identity, " + column + ", type) VALUES (?, ?, ?)";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
//            preparedStatement.setLong(1, identity.getNumber());
//            preparedStatement.setLong(2, value);
//            for (@Nonnull SemanticType type : types) {
//                preparedStatement.setLong(3, type.getNumber());
//                preparedStatement.addBatch();
//            }
//            preparedStatement.executeBatch();
//        } catch (@Nonnull SQLException exception) {
//            boolean merged = false;
//            for (@Nonnull SemanticType type : types) {
//                if (type.hasBeenMerged()) merged = true;
//            }
//            if (merged) addTypes(connection, identity, table, column, value, types);
//            else throw exception;
//        }
//    }
//    
//    /**
//     * Removes the given types with the given value in the given column from the given table of the given identity.
//     * 
//     * @param identity the identity for which the types are to be removed.
//     * @param table the name of the database table from which the types are to be removed and which has to have columns with the names 'identity', 'type' and the given column name.
//     * @param column the name of the column which has to equal the given value.
//     * @param value the value to restrict the given column for every removed type.
//     * @param types the types to be removed from the given table of the given identity.
//     * @return the number of rows deleted from the database.
//     */
//    private static int removeTypes(@Nonnull NonHostIdentity identity, @Nonnull String table, @Nonnull String column, long value, @Nonnull Set<SemanticType> types) throws SQLException {
//        @Nonnull String statement = "DELETE FROM " + table + " WHERE identity = ? AND " + column + " = ? AND type = ?";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
//            preparedStatement.setLong(1, identity.getNumber());
//            preparedStatement.setLong(2, value);
//            for (@Nonnull SemanticType type : types) {
//                preparedStatement.setLong(3, type.getNumber());
//                preparedStatement.addBatch();
//            }
//            int[] updated = preparedStatement.executeBatch();
//            
//            int sum = 0;
//            @Nonnull Set<SemanticType> merged = new LinkedHashSet<SemanticType>();
//            for (int i = 0; i < updated.length; i++) {
//                sum += updated[i];
//                if (updated[i] < 1) {
//                    @Nonnull SemanticType type = getElement(types, i);
//                    if (type.hasBeenMerged()) merged.add(type);
//                }
//            }
//            if (!merged.isEmpty()) return sum + removeTypes(connection, identity, table, column, value, merged);
//            return sum;
//        }
//    }
//    
//    /**
//     * Returns the permissions of the given context at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param context the context whose permissions are to be returned.
//     * @param inherited whether the permissions of the supercontexts are inherited.
//     * @return the permissions of the given context at the given identity.
//     * @require contextExists(connection, identity, context) : "The given context has to exist.";
//     */
//    static @Nonnull Set<SemanticType> getContextPermissions(@Nonnull NonHostIdentity identity, @Nonnull Context context, boolean inherited) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        return getTypes(connection, identity, "context_permission", "context" + (inherited ? " IN (" + context.getSupercontextsAsString() + ")" : " = " + context));
//    }
//    
//    /**
//     * Adds the given permissions to the given context at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param context the context whose permissions are extended.
//     * @param permissions the permissions to be added to the given context.
//     * @require contextExists(connection, identity, context) : "The given context has to exist.";
//     */
//    static void addContextPermissions(@Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<SemanticType> permissions) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        addTypes(connection, identity, "context_permission", "context", context.getNumber(), permissions);
//    }
//    
//    /**
//     * Removes the given permissions from the given context at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param context the context whose permissions are reduced.
//     * @param permissions the permissions to be removed from the given context.
//     * @require contextExists(connection, identity, context) : "The given context has to exist.";
//     */
//    static void removeContextPermissions(@Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<SemanticType> permissions) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        removeTypes(connection, identity, "context_permission", "context", context.getNumber(), permissions);
//    }
//    
//    /**
//     * Returns the authentications of the given context at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param context the context whose authentications are to be returned.
//     * @param inherited whether the authentications of the supercontexts are inherited.
//     * @return the authentications of the given context at the given identity.
//     * @require contextExists(connection, identity, context) : "The given context has to exist.";
//     */
//    static @Nonnull Set<SemanticType> getContextAuthentications(@Nonnull NonHostIdentity identity, @Nonnull Context context, boolean inherited) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        return getTypes(connection, identity, "context_authentication", "context" + (inherited ? " IN (" + context.getSupercontextsAsString() + ")" : " = " + context));
//    }
//    
//    /**
//     * Adds the given authentications to the given context at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param context the context whose authentications are extended.
//     * @param authentications the authentications to be added to the given context.
//     * @require contextExists(connection, identity, context) : "The given context has to exist.";
//     */
//    static void addContextAuthentications(@Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<SemanticType> authentications) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        addTypes(connection, identity, "context_authentication", "context", context.getNumber(), authentications);
//    }
//    
//    /**
//     * Removes the given authentications from the given context at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param context the context whose authentications are reduced.
//     * @param authentications the authentications to be removed from the given context.
//     * @require contextExists(connection, identity, context) : "The given context has to exist.";
//     */
//    static void removeContextAuthentications(@Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<SemanticType> authentications) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        removeTypes(connection, identity, "context_authentication", "context", context.getNumber(), authentications);
//    }
//    
//    /**
//     * Removes the given context at the given identity.
//     * 
//     * @param identity the identity of interest.
//     * @param context the context to be removed.
//     * @require contextExists(connection, identity, context) : "The given context has to exist.";
//     */
//    static void removeContext(@Nonnull NonHostIdentity identity, @Nonnull Context context) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        try (@Nonnull Statement statement = connection.createStatement()) {
//            @Nonnull String condition = "WHERE identity = " + identity + " AND context & " + context.getMask() + " = " + context;
//            statement.executeUpdate("DELETE FROM context_name " + condition);
//            statement.executeUpdate("DELETE FROM context_permission " + condition);
//            statement.executeUpdate("DELETE FROM context_authentication " + condition);
//            statement.executeUpdate("DELETE FROM context_contact " + condition);
//            
//            // Remove the preferences, permissions and authentications of all contacts that no longer have a context.
//            statement.executeUpdate("DELETE FROM contact_preference WHERE identity = " + identity + " AND NOT EXISTS (SELECT * FROM context_contact WHERE context_contact.identity = " + identity + " AND context_contact.contact = contact_preference.contact)");
//            statement.executeUpdate("DELETE FROM contact_permission WHERE identity = " + identity + " AND NOT EXISTS (SELECT * FROM context_contact WHERE context_contact.identity = " + identity + " AND context_contact.contact = contact_permission.contact)");
//            statement.executeUpdate("DELETE FROM contact_authentication WHERE identity = " + identity + " AND NOT EXISTS (SELECT * FROM context_contact WHERE context_contact.identity = " + identity + " AND context_contact.contact = contact_authentication.contact)");
//        }
//    }
//    
//    
//    /**
//     * Returns the contacts in the given context at the given identity.
//     * 
//     * @param identity the identity whose contacts are to be returned.
//     * @param context the context of the requested contacts.
//     * @param recursive whether contacts from subcontexts shall be included as well.
//     * @return the contacts in the given context at the given identity.
//     * @require contextExists(connection, identity, context) : "The given context has to exist.";
//     */
//    static @Nonnull Set<Person> getContacts(@Nonnull NonHostIdentity identity, @Nonnull Context context, boolean recursive) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        @Nonnull String query = "SELECT DISTINCT general_identity.identity, general_identity.category, general_identity.address FROM context_contact JOIN general_identity ON context_contact.contact = general_identity.identity WHERE context_contact.identity = " + identity + " AND context_contact.context" + (recursive ? " & " + context.getMask() : "") + " = " + context;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            @Nonnull Set<Person> contacts = new LinkedHashSet<Person>();
//            while (resultSet.next()) {
//                long number = resultSet.getLong(1);
//                @Nonnull Category category = Category.get(resultSet.getByte(2));
//                @Nonnull NonHostIdentifier address = new NonHostIdentifier(resultSet.getString(3));
//                contacts.add(Identity.create(category, number, address).toPerson());
//            }
//            return contacts;
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("Some values returned by the database are invalid.", exception);
//        }
//    }
//    
//    /**
//     * Adds the contacts to the given context at the given identity.
//     * 
//     * @param identity the identity to which the contacts are to be added.
//     * @param context the context to which the contacts are to be added.
//     * @param contacts the contacts to add to the given context.
//     */
//    static void addContacts(@Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<Person> contacts) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        @Nonnull String statement = "INSERT " + Database.IGNORE + " INTO context_contact (identity, context, contact) VALUES (?, ?, ?)";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
//            preparedStatement.setLong(1, identity.getNumber());
//            preparedStatement.setLong(2, context.getNumber());
//            for (@Nonnull Person contact : contacts) {
//                preparedStatement.setLong(3, contact.getNumber());
//                preparedStatement.addBatch();
//            }
//            preparedStatement.executeBatch();
//        } catch (@Nonnull SQLException exception) {
//            boolean merged = false;
//            for (@Nonnull Person contact : contacts) {
//                if (contact.hasBeenMerged()) merged = true;
//            }
//            if (merged) addContacts(connection, identity, context, contacts);
//            else throw exception;
//        }
//    }
//    
//    /**
//     * Removes the given contacts from the given context at the given identity.
//     * 
//     * @param identity the identity from which the contacts are to be removed.
//     * @param context the context whose contacts are removed.
//     * @param contacts the contacts to be removed from the given context.
//     * @require contextExists(connection, identity, context) : "The given context has to exist.";
//     */
//    static void removeContacts(@Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<Person> contacts) throws SQLException {
//        assert contextExists(connection, identity, context) : "The given context has to exist.";
//        
//        @Nonnull String sql = "DELETE FROM context_contact WHERE identity = ? AND context = ? AND contact = ?";
//        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//            preparedStatement.setLong(1, identity.getNumber());
//            preparedStatement.setLong(2, context.getNumber());
//            for (@Nonnull Person contact : contacts) {
//                preparedStatement.setLong(3, contact.getNumber());
//                preparedStatement.addBatch();
//            }
//            int[] updated = preparedStatement.executeBatch();
//            
//            @Nonnull Set<Person> merged = new LinkedHashSet<Person>();
//            for (int i = 0; i < updated.length; i++) {
//                if (updated[i] < 1) {
//                    @Nonnull Person contact = getElement(contacts, i);
//                    if (contact.hasBeenMerged()) merged.add(contact);
//                }
//            }
//            if (!merged.isEmpty()) removeContacts(connection, identity, context, merged);
//        }
//        
//        // Remove the preferences, permissions and authentications of the contacts that no longer have a context.
//        for (@Nonnull Person contact : contacts) {
//            if (getContexts(connection, identity, contact).isEmpty()) {
//                try (@Nonnull Statement statement = connection.createStatement()) {
//                    @Nonnull String condition = "WHERE identity = " + identity + " AND contact = " + contact;
//                    statement.executeUpdate("DELETE FROM contact_preference " + condition);
//                    statement.executeUpdate("DELETE FROM contact_permission " + condition);
//                    statement.executeUpdate("DELETE FROM contact_authentication " + condition);
//                }
//            }
//        }
//    }
//    
//    /**
//     * Returns the contexts of the given contact at the given identity.
//     * 
//     * @param identity the identity which has the given contact.
//     * @param contact the contact whose contexts are to be returned.
//     * @return the contexts of the given contact at the given identity.
//     */
//    static @Nonnull Set<Context> getContexts(@Nonnull NonHostIdentity identity, @Nonnull Person contact) throws SQLException {
//        @Nonnull String query = "SELECT context FROM context_contact WHERE identity = " + identity + " AND contact = " + contact;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            @Nonnull Set<Context> contexts = new LinkedHashSet<Context>();
//            while (resultSet.next()) contexts.add(new Context(resultSet.getLong(1)));
//            if (contexts.isEmpty() && contact.hasBeenMerged()) return getContexts(connection, identity, contact);
//            else return contexts;
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw new SQLException("Some values returned by the database are invalid.", exception);
//        }
//    }
//    
//    /**
//     * Returns whether the given contact is in the given context of the given identity.
//     * 
//     * @param identity the identity which has the given contact.
//     * @param contact the contact which is to be checked.
//     * @param context the context which is to be checked.
//     * @return whether the given contact is in the given context of the given identity.
//     */
//    public static boolean isInContext(@Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Context context) throws SQLException {
//        @Nonnull String query = "SELECT EXISTS (SELECT * FROM context_contact WHERE identity = " + identity + " AND context & " + context.getMask() + " = " + context + " AND contact = " + contact;
//        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
//            if (resultSet.next()) {
//                boolean result = resultSet.getBoolean(1);
//                if (!result && contact.hasBeenMerged()) return isInContext(connection, identity, contact, context);
//                else return result;
//            } else {
//                throw new SQLException("There should always be a result.");
//            }
//        }
//    }
    
}
