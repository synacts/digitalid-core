package ch.virtualid.module;

import ch.virtualid.agent.Agent;
import ch.virtualid.concepts.Context;
import ch.virtualid.database.Entity;
import ch.virtualid.exception.ShouldNeverHappenError;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.database.Database;
import ch.xdf.Block;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.javatuples.Pair;

/**
 * This class provides database access to the contexts of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Contexts extends Module {
    
    /**
     * Initializes the database by creating the appropriate tables if necessary.
     * 
     * @param connection an open client or host connection to the database.
     */
    Contexts(@Nonnull Entity connection) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS context_name (identity BIGINT NOT NULL, context BIGINT NOT NULL, name VARCHAR(50) NOT NULL COLLATE " + Database.UTF16_BIN + ", PRIMARY KEY (identity, context), FOREIGN KEY (identity) REFERENCES map_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS context_permission (identity BIGINT NOT NULL, context BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, context, type), FOREIGN KEY (identity) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS context_authentication (identity BIGINT NOT NULL, context BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (identity, context, type), FOREIGN KEY (identity) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS context_subcontext (identity BIGINT NOT NULL, context BIGINT NOT NULL, subcontext BIGINT NOT NULL, sequence " + Database.getConfiguration().TINYINT() + ", PRIMARY KEY (identity, context, subcontext), FOREIGN KEY (identity) REFERENCES map_identity (identity))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS context_contact (identity BIGINT NOT NULL, context BIGINT NOT NULL, contact BIGINT NOT NULL, PRIMARY KEY (identity, context, contact), FOREIGN KEY (identity) REFERENCES map_identity (identity), FOREIGN KEY (contact) REFERENCES map_identity (identity))");
        }
        
        Mapper.addTypeReference("context_permission", "type");
        Mapper.addTypeReference("context_authentication", "type");
        Mapper.addReference("context_contact", "contact");
    }
    
    /**
     * Returns whether the given context at the given identity exists.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity whose context is to be checked.
     * @param context the context whose existence is to be checked.
     * @return whether the given context at the given identity exists.
     */
    static boolean contextExists(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context) throws SQLException {
        @Nonnull String query = "SELECT EXISTS(SELECT * FROM context_name WHERE identity = " + identity + " AND context = " + context + ")";
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) return resultSet.getBoolean(1);
            else throw new SQLException("The executed statement should always have a result.");
        }
    }
    
    /**
     * Returns the subcontexts of the given context at the given identity (including the given context).
     * 
     * @param connection an open connection to the database.
     * @param identity the identity whose contexts are to be returned.
     * @param context the supercontext of the requested contexts.
     * @return the subcontexts of the given context at the given identity.
     * @require contextExists(connection, identity, context) : "The given context has to exist.";
     */
    static @Nonnull Set<Pair<Context, String>> getSubcontexts(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        @Nonnull String query = "SELECT context, name FROM context_name WHERE entity = " + identity + " AND context & " + context.getMask() + " = " + context;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            @Nonnull Set<Pair<Context, String>> contexts = new LinkedHashSet<Pair<Context, String>>();
            while (resultSet.next()) contexts.add(new Pair<Context, String>(new Context(resultSet.getLong(1)), resultSet.getString(2)));
            return contexts;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Returns the name of the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param context the context whose name is to be returned.
     * @return the name of the given context at the given identity.
     * @require contextExists(connection, identity, context) : "The given context has to exist.";
     */
    static @Nonnull String getContextName(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        @Nonnull String query = "SELECT name FROM context_name WHERE identity = " + identity + " AND context = " + context;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) return resultSet.getString(2);
            else throw new SQLException("The given context could not be found though it should exist.");
        }
    }
    
    /**
     * Sets the name of the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param context the context whose name is to be set.
     * @param name the name to be set.
     * @require contextExists(connection, identity, context.getSupercontext()) : "The supercontext of the given context has to exist.";
     * @require name.length() <= 50 : "The context name may have at most 50 characters.";
     */
    static void setContextName(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull String name) throws SQLException {
        assert contextExists(connection, identity, context.getSupercontext()) : "The supercontext of the given context has to exist.";
        assert name.length() <= 50 : "The context name may have at most 50 characters.";
        
        @Nonnull String statement = "REPLACE INTO context_name (identity, context, name) VALUES (?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setLong(1, identity.getNumber());
            preparedStatement.setLong(2, context.getNumber());
            preparedStatement.setString(3, name);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Returns the element with the given position from the given set.
     * 
     * @param set the set whose element is to be returned.
     * @param position the position of the element which is to be returned.
     * @return the element with the given position from the given set.
     * @require position >= 0 && position < set.size() : "The position is within the bounds of the set.";
     */
    public static <T> T getElement(@Nonnull Set<T> set, int position) {
        assert position >= 0 && position < set.size() : "The position is within the bounds of the set.";
        
        int i = 0;
        for (@Nonnull T element : set) {
            if (i == position) return element;
            i++;
        }
        
        throw new ShouldNeverHappenError("The requested element of the set could not be found.");
    }
    
    /**
     * Returns the types from the given table with the given condition of the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity whose types are to be returned.
     * @param table the name of the database table which is to be queried and which has to have a column with the name 'type'.
     * @param condition a condition to filter the rows of the given database table.
     * @return the types from the given table with the given condition of the given identity.
     */
    private static @Nonnull Set<SemanticType> getTypes(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull String table, @Nonnull String condition) throws SQLException {
        @Nonnull String query = "SELECT map_identity.identity, map_identity.category, map_identity.address FROM " + table + " JOIN map_identity ON " + table + ".type = map_identity.identity WHERE " + table + ".identity = " + identity + " AND " + table + "." + condition;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            @Nonnull Set<SemanticType> types = new LinkedHashSet<SemanticType>();
            while (resultSet.next()) {
                long number = resultSet.getLong(1);
                @Nonnull Category category = Category.get(resultSet.getByte(2));
                @Nonnull NonHostIdentifier address = new NonHostIdentifier(resultSet.getString(3));
                types.add(Identity.create(category, number, address).toSemanticType());
            }
            return types;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Adds the given types with the given value in the given column to the given table of the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity for which the types are to be added.
     * @param table the name of the database table to which the types are to be added and which has to have columns with the names 'identity', 'type' and the given column name.
     * @param column the name of the column which is to be filled with the given value.
     * @param value the value to fill into the given column for every added type.
     * @param types the types to be added to the given table of the given identity.
     */
    private static void addTypes(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull String table, @Nonnull String column, long value, @Nonnull Set<SemanticType> types) throws SQLException {
        @Nonnull String statement = "INSERT " + Database.IGNORE + " INTO " + table + " (identity, " + column + ", type) VALUES (?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setLong(1, identity.getNumber());
            preparedStatement.setLong(2, value);
            for (@Nonnull SemanticType type : types) {
                preparedStatement.setLong(3, type.getNumber());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (@Nonnull SQLException exception) {
            boolean merged = false;
            for (@Nonnull SemanticType type : types) {
                if (type.hasBeenMerged()) merged = true;
            }
            if (merged) addTypes(connection, identity, table, column, value, types);
            else throw exception;
        }
    }
    
    /**
     * Removes the given types with the given value in the given column from the given table of the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity for which the types are to be removed.
     * @param table the name of the database table from which the types are to be removed and which has to have columns with the names 'identity', 'type' and the given column name.
     * @param column the name of the column which has to equal the given value.
     * @param value the value to restrict the given column for every removed type.
     * @param types the types to be removed from the given table of the given identity.
     * @return the number of rows deleted from the database.
     */
    private static int removeTypes(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull String table, @Nonnull String column, long value, @Nonnull Set<SemanticType> types) throws SQLException {
        @Nonnull String statement = "DELETE FROM " + table + " WHERE identity = ? AND " + column + " = ? AND type = ?";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setLong(1, identity.getNumber());
            preparedStatement.setLong(2, value);
            for (@Nonnull SemanticType type : types) {
                preparedStatement.setLong(3, type.getNumber());
                preparedStatement.addBatch();
            }
            int[] updated = preparedStatement.executeBatch();
            
            int sum = 0;
            @Nonnull Set<SemanticType> merged = new LinkedHashSet<SemanticType>();
            for (int i = 0; i < updated.length; i++) {
                sum += updated[i];
                if (updated[i] < 1) {
                    @Nonnull SemanticType type = getElement(types, i);
                    if (type.hasBeenMerged()) merged.add(type);
                }
            }
            if (!merged.isEmpty()) return sum + removeTypes(connection, identity, table, column, value, merged);
            return sum;
        }
    }
    
    /**
     * Returns the permissions of the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param context the context whose permissions are to be returned.
     * @param inherited whether the permissions of the supercontexts are inherited.
     * @return the permissions of the given context at the given identity.
     * @require contextExists(connection, identity, context) : "The given context has to exist.";
     */
    static @Nonnull Set<SemanticType> getContextPermissions(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context, boolean inherited) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        return getTypes(connection, identity, "context_permission", "context" + (inherited ? " IN (" + context.getSupercontextsAsString() + ")" : " = " + context));
    }
    
    /**
     * Adds the given permissions to the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param context the context whose permissions are extended.
     * @param permissions the permissions to be added to the given context.
     * @require contextExists(connection, identity, context) : "The given context has to exist.";
     */
    static void addContextPermissions(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<SemanticType> permissions) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        addTypes(connection, identity, "context_permission", "context", context.getNumber(), permissions);
    }
    
    /**
     * Removes the given permissions from the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param context the context whose permissions are reduced.
     * @param permissions the permissions to be removed from the given context.
     * @require contextExists(connection, identity, context) : "The given context has to exist.";
     */
    static void removeContextPermissions(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<SemanticType> permissions) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        removeTypes(connection, identity, "context_permission", "context", context.getNumber(), permissions);
    }
    
    /**
     * Returns the authentications of the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param context the context whose authentications are to be returned.
     * @param inherited whether the authentications of the supercontexts are inherited.
     * @return the authentications of the given context at the given identity.
     * @require contextExists(connection, identity, context) : "The given context has to exist.";
     */
    static @Nonnull Set<SemanticType> getContextAuthentications(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context, boolean inherited) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        return getTypes(connection, identity, "context_authentication", "context" + (inherited ? " IN (" + context.getSupercontextsAsString() + ")" : " = " + context));
    }
    
    /**
     * Adds the given authentications to the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param context the context whose authentications are extended.
     * @param authentications the authentications to be added to the given context.
     * @require contextExists(connection, identity, context) : "The given context has to exist.";
     */
    static void addContextAuthentications(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<SemanticType> authentications) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        addTypes(connection, identity, "context_authentication", "context", context.getNumber(), authentications);
    }
    
    /**
     * Removes the given authentications from the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param context the context whose authentications are reduced.
     * @param authentications the authentications to be removed from the given context.
     * @require contextExists(connection, identity, context) : "The given context has to exist.";
     */
    static void removeContextAuthentications(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<SemanticType> authentications) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        removeTypes(connection, identity, "context_authentication", "context", context.getNumber(), authentications);
    }
    
    /**
     * Removes the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity of interest.
     * @param context the context to be removed.
     * @require contextExists(connection, identity, context) : "The given context has to exist.";
     */
    static void removeContext(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        try (@Nonnull Statement statement = connection.createStatement()) {
            @Nonnull String condition = "WHERE identity = " + identity + " AND context & " + context.getMask() + " = " + context;
            statement.executeUpdate("DELETE FROM context_name " + condition);
            statement.executeUpdate("DELETE FROM context_permission " + condition);
            statement.executeUpdate("DELETE FROM context_authentication " + condition);
            statement.executeUpdate("DELETE FROM context_contact " + condition);
            
            // Remove the preferences, permissions and authentications of all contacts that no longer have a context.
            statement.executeUpdate("DELETE FROM contact_preference WHERE identity = " + identity + " AND NOT EXISTS (SELECT * FROM context_contact WHERE context_contact.identity = " + identity + " AND context_contact.contact = contact_preference.contact)");
            statement.executeUpdate("DELETE FROM contact_permission WHERE identity = " + identity + " AND NOT EXISTS (SELECT * FROM context_contact WHERE context_contact.identity = " + identity + " AND context_contact.contact = contact_permission.contact)");
            statement.executeUpdate("DELETE FROM contact_authentication WHERE identity = " + identity + " AND NOT EXISTS (SELECT * FROM context_contact WHERE context_contact.identity = " + identity + " AND context_contact.contact = contact_authentication.contact)");
        }
    }
    
    
    /**
     * Returns the contacts in the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity whose contacts are to be returned.
     * @param context the context of the requested contacts.
     * @param recursive whether contacts from subcontexts shall be included as well.
     * @return the contacts in the given context at the given identity.
     * @require contextExists(connection, identity, context) : "The given context has to exist.";
     */
    static @Nonnull Set<Person> getContacts(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context, boolean recursive) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        @Nonnull String query = "SELECT DISTINCT map_identity.identity, map_identity.category, map_identity.address FROM context_contact JOIN map_identity ON context_contact.contact = map_identity.identity WHERE context_contact.identity = " + identity + " AND context_contact.context" + (recursive ? " & " + context.getMask() : "") + " = " + context;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            @Nonnull Set<Person> contacts = new LinkedHashSet<Person>();
            while (resultSet.next()) {
                long number = resultSet.getLong(1);
                @Nonnull Category category = Category.get(resultSet.getByte(2));
                @Nonnull NonHostIdentifier address = new NonHostIdentifier(resultSet.getString(3));
                contacts.add(Identity.create(category, number, address).toPerson());
            }
            return contacts;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Adds the contacts to the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity to which the contacts are to be added.
     * @param context the context to which the contacts are to be added.
     * @param contacts the contacts to add to the given context.
     */
    static void addContacts(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<Person> contacts) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        @Nonnull String statement = "INSERT " + Database.IGNORE + " INTO context_contact (identity, context, contact) VALUES (?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setLong(1, identity.getNumber());
            preparedStatement.setLong(2, context.getNumber());
            for (@Nonnull Person contact : contacts) {
                preparedStatement.setLong(3, contact.getNumber());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (@Nonnull SQLException exception) {
            boolean merged = false;
            for (@Nonnull Person contact : contacts) {
                if (contact.hasBeenMerged()) merged = true;
            }
            if (merged) addContacts(connection, identity, context, contacts);
            else throw exception;
        }
    }
    
    /**
     * Removes the given contacts from the given context at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity from which the contacts are to be removed.
     * @param context the context whose contacts are removed.
     * @param contacts the contacts to be removed from the given context.
     * @require contextExists(connection, identity, context) : "The given context has to exist.";
     */
    static void removeContacts(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Context context, @Nonnull Set<Person> contacts) throws SQLException {
        assert contextExists(connection, identity, context) : "The given context has to exist.";
        
        @Nonnull String sql = "DELETE FROM context_contact WHERE identity = ? AND context = ? AND contact = ?";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, identity.getNumber());
            preparedStatement.setLong(2, context.getNumber());
            for (@Nonnull Person contact : contacts) {
                preparedStatement.setLong(3, contact.getNumber());
                preparedStatement.addBatch();
            }
            int[] updated = preparedStatement.executeBatch();
            
            @Nonnull Set<Person> merged = new LinkedHashSet<Person>();
            for (int i = 0; i < updated.length; i++) {
                if (updated[i] < 1) {
                    @Nonnull Person contact = getElement(contacts, i);
                    if (contact.hasBeenMerged()) merged.add(contact);
                }
            }
            if (!merged.isEmpty()) removeContacts(connection, identity, context, merged);
        }
        
        // Remove the preferences, permissions and authentications of the contacts that no longer have a context.
        for (@Nonnull Person contact : contacts) {
            if (getContexts(connection, identity, contact).isEmpty()) {
                try (@Nonnull Statement statement = connection.createStatement()) {
                    @Nonnull String condition = "WHERE identity = " + identity + " AND contact = " + contact;
                    statement.executeUpdate("DELETE FROM contact_preference " + condition);
                    statement.executeUpdate("DELETE FROM contact_permission " + condition);
                    statement.executeUpdate("DELETE FROM contact_authentication " + condition);
                }
            }
        }
    }
    
    /**
     * Returns the contexts of the given contact at the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity which has the given contact.
     * @param contact the contact whose contexts are to be returned.
     * @return the contexts of the given contact at the given identity.
     */
    static @Nonnull Set<Context> getContexts(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Person contact) throws SQLException {
        @Nonnull String query = "SELECT context FROM context_contact WHERE identity = " + identity + " AND contact = " + contact;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            @Nonnull Set<Context> contexts = new LinkedHashSet<Context>();
            while (resultSet.next()) contexts.add(new Context(resultSet.getLong(1)));
            if (contexts.isEmpty() && contact.hasBeenMerged()) return getContexts(connection, identity, contact);
            else return contexts;
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Returns whether the given contact is in the given context of the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity which has the given contact.
     * @param contact the contact which is to be checked.
     * @param context the context which is to be checked.
     * @return whether the given contact is in the given context of the given identity.
     */
    public static boolean isInContext(@Nonnull Entity connection, @Nonnull NonHostIdentity identity, @Nonnull Person contact, @Nonnull Context context) throws SQLException {
        @Nonnull String query = "SELECT EXISTS (SELECT * FROM context_contact WHERE identity = " + identity + " AND context & " + context.getMask() + " = " + context + " AND contact = " + contact;
        try (@Nonnull Statement statement = connection.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                boolean result = resultSet.getBoolean(1);
                if (!result && contact.hasBeenMerged()) return isInContext(connection, identity, contact, context);
                else return result;
            } else {
                throw new SQLException("There should always be a result.");
            }
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
    protected void addAll(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull Block block) throws SQLException {
        
    }
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose entries are to be removed.
     */
    @Override
    protected void removeAll(@Nonnull Entity connection, @Nonnull Entity entity) throws SQLException {
        
    }
    
}
