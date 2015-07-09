package net.digitalid.core.contact;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadonlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadonlyArray;
import net.digitalid.core.collections.ReadonlyList;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.EntityClass;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.IdentityClass;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.identity.Person;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.StringWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class provides database access to the {@link Context contexts} of the core service.
 * 
 * Big Problem: How do agents learn when a context above the one they are authorized for receives a new preference, permission or authentication?
 * Possible Solutions:
 * – They don't (and thus get out of synch and need to reload from time to time). => Best option!
 * – Agents always see the full context structure but without the contacts (and possibly names).
 * – Agents cannot be restricted to a specific context and see thus always everything (including the contacts).
 * 
 * What happens when a context is removed and an outgoing role or a client depends on one of its subcontexts (including itself)?
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.1
 */
public final class ContextModule implements BothModule {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module Initialization –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Initializes this class.
     */
    public static void initialize() {}
    
    /**
     * Stores an instance of this module.
     */
    static final ContextModule MODULE = new ContextModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Table Creation and Deletion –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates the table which is referenced for the given site.
     * 
     * @param site the site for which the reference table is created.
     */
    @NonCommitting
    public static void createReferenceTable(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "context_name (entity " + EntityClass.FORMAT + " NOT NULL, context " + Context.FORMAT + " NOT NULL, name VARCHAR(50) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", PRIMARY KEY (entity, context), FOREIGN KEY (entity) " + site.getEntityReference() + ")");
        }
    }
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "context_preference (entity " + EntityClass.FORMAT + " NOT NULL, context " + Context.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, PRIMARY KEY (entity, context, type), FOREIGN KEY (entity, context) " + Context.getReference(site) + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "context_permission (entity " + EntityClass.FORMAT + " NOT NULL, context " + Context.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, PRIMARY KEY (entity, context, type), FOREIGN KEY (entity, context) " + Context.getReference(site) + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "context_authentication (entity " + EntityClass.FORMAT + " NOT NULL, context " + Context.FORMAT + " NOT NULL, type " + Mapper.FORMAT + " NOT NULL, PRIMARY KEY (entity, context, type), FOREIGN KEY (entity, context) " + Context.getReference(site) + ", FOREIGN KEY (type) " + Mapper.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "context_subcontext (entity " + EntityClass.FORMAT + " NOT NULL, context " + Context.FORMAT + " NOT NULL, subcontext " + Context.FORMAT + " NOT NULL, sequence SMALLINT, PRIMARY KEY (entity, context, subcontext), FOREIGN KEY (entity, context) " + Context.getReference(site) + ", FOREIGN KEY (entity, subcontext) " + Context.getReference(site) + ")");
            // TODO: Drop the sequence number and include a counter for how many times a subcontext is contained in a context, which is raised and lowered accordingly. (Zero means it's not a subcontext and a separate boolean indicates whether it's a direct subcontext.) Contexts form a DAG, right? Something like an order number that allows gaps might still be nice.
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "context_contact (entity " + EntityClass.FORMAT + " NOT NULL, context " + Context.FORMAT + " NOT NULL, contact " + Contact.FORMAT + " NOT NULL, PRIMARY KEY (entity, context, contact), FOREIGN KEY (entity, context) " + Context.getReference(site) + ", FOREIGN KEY (contact) " + Contact.REFERENCE + ")");
            Mapper.addReference(site + "context_contact", "contact", "entity", "context", "contact");
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Mapper.removeReference(site + "context_contact", "contact", "entity", "context", "contact");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "context_contact");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "context_subcontext");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "context_authentication");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "context_permission");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "context_preference");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "context_name");
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module Export and Import –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code entry.context.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.create("entry.context.module@core.digitalid.net").load(TupleWrapper.TYPE, net.digitalid.core.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code context.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.create("context.module@core.digitalid.net").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve all the entries from the database table(s).
        }
        return new ListWrapper(MODULE_FORMAT, entries.freeze()).toBlock();
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        final @Nonnull ReadonlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
        for (final @Nonnull Block entry : entries) {
            // TODO: Add all entries to the database table(s).
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State Getter and Setter –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code entry.name.context.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NAME_STATE_ENTRY = SemanticType.create("entry.name.context.state@core.digitalid.net").load(TupleWrapper.TYPE, Context.TYPE, Context.NAME_TYPE);
    
    /**
     * Stores the semantic type {@code table.name.context.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NAME_STATE_TABLE = SemanticType.create("table.name.context.state@core.digitalid.net").load(ListWrapper.TYPE, NAME_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.contact.context.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CONTACT_STATE_ENTRY = SemanticType.create("entry.contact.context.state@core.digitalid.net").load(TupleWrapper.TYPE, Context.TYPE, Person.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code table.contact.context.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CONTACT_STATE_TABLE = SemanticType.create("table.contact.context.state@core.digitalid.net").load(ListWrapper.TYPE, CONTACT_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code context.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.create("context.state@core.digitalid.net").load(TupleWrapper.TYPE, NAME_STATE_TABLE, CONTACT_STATE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadonlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        // TODO: Extend this implementation and make sure the state is restricted according to the restrictions.
        // TODO: This might be a little bit complicated as the context for which the client is authorized needs to be aggregated.
        final @Nonnull Site site = entity.getSite();
        final @Nonnull FreezableArray<Block> tables = new FreezableArray<>(2);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT context, name FROM " + site + "context_name WHERE entity = " + entity)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Context context = Context.getNotNull(entity, resultSet, 1);
                    final @Nonnull String name = resultSet.getString(2);
                    entries.add(new TupleWrapper(NAME_STATE_ENTRY, context, new StringWrapper(Context.NAME_TYPE, name)).toBlock());
                }
                tables.set(0, new ListWrapper(NAME_STATE_TABLE, entries.freeze()).toBlock());
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT context, contact FROM " + site + "context_contact WHERE entity = " + entity)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Context context = Context.getNotNull(entity, resultSet, 1);
                    final @Nonnull Identity person = IdentityClass.getNotNull(resultSet, 2);
                    entries.add(new TupleWrapper(CONTACT_STATE_ENTRY, context, person.toBlockable(Person.IDENTIFIER)).toBlock());
                }
                tables.set(1, new ListWrapper(CONTACT_STATE_TABLE, entries.freeze()).toBlock());
            }
            
        }
        return new TupleWrapper(STATE_FORMAT, tables.freeze()).toBlock();
    }
    
    @Override
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
        
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertIgnore(statement, site + "context_name", "entity", "context");
            Database.onInsertIgnore(statement, site + "context_contact", "entity", "context", "contact");
        }
        
        final @Nonnull ReadonlyArray<Block> tables = new TupleWrapper(block).getElementsNotNull(2);
        final @Nonnull String prefix = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + site;
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "context_name (entity, context, name) VALUES (?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(0)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(3);
                Context.get(entity, elements.getNotNull(0)).set(preparedStatement, 2);
                preparedStatement.setString(3, new StringWrapper(elements.getNotNull(1)).getString());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "context_contact (entity, context, contact) VALUES (?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(tables.getNotNull(1)).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(entry).getElementsNotNull(2);
                Context.get(entity, elements.getNotNull(0)).set(preparedStatement, 2);
                IdentityClass.create(elements.getNotNull(1)).toPerson().set(preparedStatement, 3);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotIgnore(statement, site + "context_name");
            Database.onInsertNotIgnore(statement, site + "context_contact");
        }
        
        Context.reset(entity);
    }
    
    @Override
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws SQLException {
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + site + "context_contact WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "context_name WHERE entity = " + entity);
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Creation –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates the given context.
     * 
     * @param context the context to be created.
     */
    @NonCommitting
    public static void create(@Nonnull Context context) throws SQLException {
        final @Nonnull String SQL = "INSERT INTO " + context.getEntity().getSite() + "context_name (entity, context, name) VALUES (?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            context.getEntity().set(preparedStatement, 1);
            context.set(preparedStatement, 2);
            preparedStatement.setString(3, "New Context");
            if (preparedStatement.executeUpdate() == 0) throw new SQLException("The context with the number " + context + " could not be created.");
        }
        // TODO: Do it correctly!
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO " + context.getEntity().getSite() + "context_subcontext (entity, context, subcontext) VALUES (" + context.getEntity() + ", " + context + ", " + context + ")");
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Contacts –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the contacts of the given context.
     * 
     * @param context the context whose contacts are to be returned.
     * 
     * @return the contacts of the given context.
     */
    @Pure
    @NonCommitting
    static @Capturable @Nonnull @NonFrozen Contacts getContacts(@Nonnull Context context) throws SQLException {
        final @Nonnull NonHostEntity entity = context.getEntity();
        final @Nonnull String SQL = "SELECT contact FROM " + entity.getSite() + "context_contact WHERE entity = " + entity + " AND context = " + context;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            return Contacts.get(entity, resultSet, 1);
        }
    }
    
    /**
     * Adds the given contacts to the given context.
     * 
     * @param context the context to which the contacts are to be added.
     * @param contacts the contacts to be added to the given context.
     */
    @NonCommitting
    static void addContacts(@Nonnull Context context, @Nonnull @Frozen ReadonlyContacts contacts) throws SQLException {
        final @Nonnull NonHostEntity entity = context.getEntity();
        final @Nonnull String SQL = "INSERT INTO " + entity.getSite() + "context_contact (entity, context, contact) VALUES (" + entity + ", " + context + ", ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            contacts.set(preparedStatement, 1);
            preparedStatement.executeBatch();
        }
    }
    
    /**
     * Removes the given contacts from the given context.
     * 
     * @param context the context from which the contacts are to be removed.
     * @param contacts the contacts to be removed from the given context.
     */
    @NonCommitting
    static void removeContacts(@Nonnull Context context, @Nonnull @Frozen ReadonlyContacts contacts) throws SQLException {
        final @Nonnull NonHostEntity entity = context.getEntity();
        final @Nonnull String SQL = "DELETE FROM " + entity.getSite() + "context_contact WHERE entity = " + entity + " AND context = " + context + " AND contact = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            contacts.set(preparedStatement, 1);
            final int[] counts = preparedStatement.executeBatch();
            for (final int count : counts) if (count < 1) throw new SQLException("Could not find a contact.");
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Legacy Code –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
//    /**
//     * Returns whether the given context at the given identity exists.
//     * 
//     * @param identity the identity whose context is to be checked.
//     * @param context the context whose existence is to be checked.
//     * @return whether the given context at the given identity exists.
//     */
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
//    @DoesNotCommit
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
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
