package net.digitalid.core.agent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.freezable.Frozen;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.validation.annotations.reference.Capturable;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.table.Site;

import net.digitalid.core.service.CoreService;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;

import net.digitalid.core.conversion.wrappers.value.BooleanWrapper;

import net.digitalid.core.conversion.wrappers.value.integer.Integer64Wrapper;

import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;

import net.digitalid.core.contact.Contact;
import net.digitalid.core.context.Context;
import net.digitalid.core.context.ContextModule;

import net.digitalid.service.core.dataservice.StateModule;

import net.digitalid.core.entity.EntityImplementation;
import net.digitalid.core.entity.NonHostAccount;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.NonNativeRole;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.IdentityImplementation;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.resolution.Mapper;

import net.digitalid.core.client.Client;
import net.digitalid.core.client.Commitment;
import net.digitalid.core.host.Host;

import net.digitalid.core.state.Service;

/**
 * This class provides database access to the {@link Agent agents} of the core service.
 */
@Stateless
public final class AgentModule implements StateModule {
    
    /* -------------------------------------------------- Module Initialization -------------------------------------------------- */
    
    static { ContextModule.initialize(); }
    
    /**
     * Stores an instance of this module.
     */
    public static final AgentModule MODULE = new AgentModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    /* -------------------------------------------------- Table Creation and Deletion -------------------------------------------------- */
    
    /**
     * Creates the table which is referenced for the given site.
     * 
     * @param site the site for which the reference table is created.
     */
    @NonCommitting
    public static void createReferenceTable(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent (entity " + EntityImplementation.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, client BOOLEAN NOT NULL, removed BOOLEAN NOT NULL, PRIMARY KEY (entity, agent), FOREIGN KEY (entity) " + site.getEntityReference() + ")");
        }
    }
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_permission (entity " + EntityImplementation.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, " + FreezableAgentPermissions.FORMAT_NOT_NULL + ", PRIMARY KEY (entity, agent, type), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", " + FreezableAgentPermissions.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_permission_order (entity " + EntityImplementation.FORMAT + " NOT NULL, stronger " + Agent.FORMAT + " NOT NULL, weaker " + Agent.FORMAT + " NOT NULL, PRIMARY KEY (entity, stronger, weaker), FOREIGN KEY (entity, stronger) " + Agent.getReference(site) + ", FOREIGN KEY (entity, weaker) " + Agent.getReference(site) + ")");
            
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_restrictions (entity " + EntityImplementation.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, " + Restrictions.FORMAT + ", PRIMARY KEY (entity, agent), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", " + Restrictions.getForeignKeys(site) + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "agent_restrictions_ord (entity " + EntityImplementation.FORMAT + " NOT NULL, stronger " + Agent.FORMAT + " NOT NULL, weaker " + Agent.FORMAT + " NOT NULL, PRIMARY KEY (entity, stronger, weaker), FOREIGN KEY (entity, stronger) " + Agent.getReference(site) + ", FOREIGN KEY (entity, weaker) " + Agent.getReference(site) + ")");
            Mapper.addReference(site + "agent_restrictions", "contact"); // TODO: The column 'contact' does not belong to a unique key constraint. Remove this line?
            
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "client_agent (entity " + EntityImplementation.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, " + Commitment.FORMAT + ", name VARCHAR(50) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", PRIMARY KEY (entity, agent), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", " + Commitment.REFERENCE + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "outgoing_role (entity " + EntityImplementation.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, relation " + Mapper.FORMAT + " NOT NULL, context " + Context.FORMAT + " NOT NULL, PRIMARY KEY (entity, agent), FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ", FOREIGN KEY (relation) " + Mapper.REFERENCE + ", FOREIGN KEY (entity, context) " + Context.getReference(site) + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "incoming_role (entity " + EntityImplementation.FORMAT + " NOT NULL, issuer " + Mapper.FORMAT + " NOT NULL, relation " + Mapper.FORMAT + " NOT NULL, agent " + Agent.FORMAT + " NOT NULL, PRIMARY KEY (entity, issuer, relation), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (issuer) " + Mapper.REFERENCE + ", FOREIGN KEY (relation) " + Mapper.REFERENCE + ")");
            Mapper.addReference(site + "incoming_role", "issuer", "entity", "issuer", "relation");
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Mapper.removeReference(site + "incoming_role", "issuer", "entity", "issuer", "relation");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "incoming_role");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "outgoing_role");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "client");
            
            Mapper.removeReference(site + "agent_restrictions", "contact");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "agent_restrictions_ord");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "agent_restrictions");
            
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "agent_permission_order");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "agent_permission");
            
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "agent");
        }
    }
    
    /* -------------------------------------------------- Module Export and Import -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code entry.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_MODULE_ENTRY = SemanticType.map("entry.agent.agent.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, Agent.NUMBER, Agent.CLIENT, Agent.REMOVED);
    
    /**
     * Stores the semantic type {@code table.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_MODULE_TABLE = SemanticType.map("table.agent.agent.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, AGENT_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.permission.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_MODULE_ENTRY = SemanticType.map("entry.permission.agent.agent.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, Agent.NUMBER, FreezableAgentPermissions.TYPE);
    
    /**
     * Stores the semantic type {@code table.permission.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_MODULE_TABLE = SemanticType.map("table.permission.agent.agent.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, AGENT_PERMISSION_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code stronger.order.permission.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_ORDER_STRONGER = SemanticType.map("stronger.order.permission.agent.agent.module@core.digitalid.net").load(Agent.NUMBER);
    
    /**
     * Stores the semantic type {@code weaker.order.permission.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_ORDER_WEAKER = SemanticType.map("weaker.order.permission.agent.agent.module@core.digitalid.net").load(Agent.NUMBER);
    
    /**
     * Stores the semantic type {@code entry.order.permission.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_ORDER_MODULE_ENTRY = SemanticType.map("entry.order.permission.agent.agent.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, AGENT_PERMISSION_ORDER_STRONGER, AGENT_PERMISSION_ORDER_WEAKER);
    
    /**
     * Stores the semantic type {@code table.order.permission.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_ORDER_MODULE_TABLE = SemanticType.map("table.order.permission.agent.agent.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, AGENT_PERMISSION_ORDER_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.restrictions.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_MODULE_ENTRY = SemanticType.map("entry.restrictions.agent.agent.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, Agent.NUMBER, Restrictions.TYPE);
    
    /**
     * Stores the semantic type {@code table.restrictions.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_MODULE_TABLE = SemanticType.map("table.restrictions.agent.agent.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, AGENT_RESTRICTIONS_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code strong.order.restrictions.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_ORDER_STRONGER = SemanticType.map("strong.order.restrictions.agent.agent.module@core.digitalid.net").load(Agent.NUMBER);
    
    /**
     * Stores the semantic type {@code weaker.order.restrictions.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_ORDER_WEAKER = SemanticType.map("weaker.order.restrictions.agent.agent.module@core.digitalid.net").load(Agent.NUMBER);
    
    /**
     * Stores the semantic type {@code entry.order.restrictions.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_ORDER_MODULE_ENTRY = SemanticType.map("entry.order.restrictions.agent.agent.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, AGENT_RESTRICTIONS_ORDER_STRONGER, AGENT_RESTRICTIONS_ORDER_WEAKER);
    
    /**
     * Stores the semantic type {@code table.order.restrictions.agent.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_ORDER_MODULE_TABLE = SemanticType.map("table.order.restrictions.agent.agent.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, AGENT_RESTRICTIONS_ORDER_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.client.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CLIENT_MODULE_ENTRY = SemanticType.map("entry.client.agent.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, Agent.NUMBER, Commitment.TYPE, Client.NAME);
    
    /**
     * Stores the semantic type {@code table.client.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CLIENT_MODULE_TABLE = SemanticType.map("table.client.agent.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, CLIENT_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.outgoing.role.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OUTGOING_ROLE_MODULE_ENTRY = SemanticType.map("entry.outgoing.role.agent.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, Agent.NUMBER, NonNativeRole.RELATION, Context.TYPE);
    
    /**
     * Stores the semantic type {@code table.outgoing.role.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OUTGOING_ROLE_MODULE_TABLE = SemanticType.map("table.outgoing.role.agent.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, OUTGOING_ROLE_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.incoming.role.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType INCOMING_ROLE_MODULE_ENTRY = SemanticType.map("entry.incoming.role.agent.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Identity.IDENTIFIER, Role.ISSUER, Role.RELATION, Role.AGENT);
    
    /**
     * Stores the semantic type {@code table.incoming.role.agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType INCOMING_ROLE_MODULE_TABLE = SemanticType.map("table.incoming.role.agent.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, INCOMING_ROLE_MODULE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code agent.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("agent.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, AGENT_MODULE_TABLE, AGENT_PERMISSION_MODULE_TABLE, AGENT_PERMISSION_ORDER_MODULE_TABLE, AGENT_RESTRICTIONS_MODULE_TABLE, AGENT_RESTRICTIONS_ORDER_MODULE_TABLE, CLIENT_MODULE_TABLE, INCOMING_ROLE_MODULE_TABLE, OUTGOING_ROLE_MODULE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws DatabaseException {
        final @Nonnull FreezableArray<Block> tables = FreezableArray.get(8);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, agent, client, removed FROM " + host + "agent")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, 1);
                    final long number = resultSet.getLong(2);
                    final boolean client = resultSet.getBoolean(3);
                    final boolean removed = resultSet.getBoolean(4);
                    entries.add(TupleWrapper.encode(AGENT_MODULE_ENTRY, identity, Integer64Wrapper.encode(Agent.NUMBER, number), BooleanWrapper.encode(Agent.CLIENT, client), BooleanWrapper.encode(Agent.REMOVED, removed)));
                }
                tables.set(0, ListWrapper.encode(AGENT_MODULE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, agent, " + FreezableAgentPermissions.COLUMNS + " FROM " + host + "agent_permission")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, 1);
                    final long number = resultSet.getLong(2);
                    final @Nonnull FreezableAgentPermissions permissions = FreezableAgentPermissions.getEmptyOrSingle(resultSet, 3);
                    entries.add(TupleWrapper.encode(AGENT_PERMISSION_MODULE_ENTRY, identity, Integer64Wrapper.encode(Agent.NUMBER, number), permissions));
                }
                tables.set(1, ListWrapper.encode(AGENT_PERMISSION_MODULE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, stronger, weaker FROM " + host + "agent_permission_order")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, 1);
                    final long stronger = resultSet.getLong(2);
                    final long weaker = resultSet.getLong(3);
                    entries.add(TupleWrapper.encode(AGENT_PERMISSION_ORDER_MODULE_ENTRY, identity, Integer64Wrapper.encode(AGENT_PERMISSION_ORDER_STRONGER, stronger), Integer64Wrapper.encode(AGENT_PERMISSION_ORDER_WEAKER, weaker)));
                }
                tables.set(2, ListWrapper.encode(AGENT_PERMISSION_ORDER_MODULE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, agent, " + Restrictions.COLUMNS + " FROM " + host + "agent_restrictions")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull NonHostAccount account = NonHostAccount.getNotNull(host, resultSet, 1);
                    final long number = resultSet.getLong(2);
                    final @Nonnull Restrictions restrictions = Restrictions.get(account, resultSet, 3);
                    entries.add(TupleWrapper.encode(AGENT_RESTRICTIONS_MODULE_ENTRY, account.getIdentity(), Integer64Wrapper.encode(Agent.NUMBER, number), restrictions));
                }
                tables.set(3, ListWrapper.encode(AGENT_RESTRICTIONS_MODULE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, stronger, weaker FROM " + host + "agent_restrictions_ord")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, 1);
                    final long stronger = resultSet.getLong(2);
                    final long weaker = resultSet.getLong(3);
                    entries.add(TupleWrapper.encode(AGENT_RESTRICTIONS_ORDER_MODULE_ENTRY, identity, Integer64Wrapper.encode(AGENT_RESTRICTIONS_ORDER_STRONGER, stronger), Integer64Wrapper.encode(AGENT_RESTRICTIONS_ORDER_WEAKER, weaker)));
                }
                tables.set(4, ListWrapper.encode(AGENT_RESTRICTIONS_ORDER_MODULE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, agent, " + Commitment.COLUMNS + ", name FROM " + host + "client")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, 1);
                    final long number = resultSet.getLong(2);
                    final @Nonnull Commitment commitment = Commitment.get(resultSet, 3);
                    final @Nonnull String name = resultSet.getString(6);
                    entries.add(TupleWrapper.encode(CLIENT_MODULE_ENTRY, identity, Integer64Wrapper.encode(Agent.NUMBER, number), commitment, StringWrapper.encodeNonNullable(Client.NAME, name)));
                }
                tables.set(5, ListWrapper.encode(CLIENT_MODULE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, agent, relation, context FROM " + host + "outgoing_role")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull NonHostAccount account = NonHostAccount.getNotNull(host, resultSet, 1);
                    final long number = resultSet.getLong(2);
                    final @Nonnull Identity relation = IdentityImplementation.getNotNull(resultSet, 3);
                    final @Nonnull Context context = Context.getNotNull(account, resultSet, 4);
                    entries.add(TupleWrapper.encode(OUTGOING_ROLE_MODULE_ENTRY, account.getIdentity(), Integer64Wrapper.encode(Agent.NUMBER, number), relation.toBlockable(Role.RELATION), context));
                }
                tables.set(6, ListWrapper.encode(OUTGOING_ROLE_MODULE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT entity, issuer, relation, agent FROM " + host + "incoming_role")) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, 1);
                    final @Nonnull Identity issuer = IdentityImplementation.getNotNull(resultSet, 2);
                    final @Nonnull Identity relation = IdentityImplementation.getNotNull(resultSet, 3);
                    final long number = resultSet.getLong(4);
                    entries.add(TupleWrapper.encode(INCOMING_ROLE_MODULE_ENTRY, identity, issuer.toBlockable(Role.ISSUER), relation.toBlockable(Role.RELATION), Integer64Wrapper.encode(Role.AGENT, number)));
                }
                tables.set(7, ListWrapper.encode(INCOMING_ROLE_MODULE_TABLE, entries.freeze()));
            }
            
        }
        return TupleWrapper.encode(MODULE_FORMAT, tables.freeze());
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        Require.that(block.getType().isBasedOn(getModuleFormat())).orThrow("The block is based on the format of this module.");
        
        final @Nonnull ReadOnlyArray<Block> tables = TupleWrapper.decode(block).getNonNullableElements(8);
        final @Nonnull String prefix = "INSERT INTO " + host;
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "agent (entity, agent, client, removed) VALUES (?, ?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(0));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(4);
                IdentityImplementation.create(elements.getNonNullable(0)).castTo(InternalNonHostIdentity.class).set(preparedStatement, 1);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(1)));
                preparedStatement.setBoolean(3, BooleanWrapper.decode(elements.getNonNullable(2)));
                preparedStatement.setBoolean(4, BooleanWrapper.decode(elements.getNonNullable(3)));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "agent_permission (entity, agent, " + FreezableAgentPermissions.COLUMNS + ") VALUES (?, ?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(1));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                IdentityImplementation.create(elements.getNonNullable(0)).castTo(InternalNonHostIdentity.class).set(preparedStatement, 1);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(1)));
                new FreezableAgentPermissions(elements.getNonNullable(2)).checkIsSingle().setEmptyOrSingle(preparedStatement, 3);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "agent_permission_order (entity, stronger, weaker) VALUES (?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(2));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                IdentityImplementation.create(elements.getNonNullable(0)).castTo(InternalNonHostIdentity.class).set(preparedStatement, 1);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(1)));
                preparedStatement.setLong(3, Integer64Wrapper.decode(elements.getNonNullable(2)));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "agent_restrictions (entity, agent, " + Restrictions.COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(3));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                final @Nonnull InternalNonHostIdentity identity = IdentityImplementation.create(elements.getNonNullable(0)).castTo(InternalNonHostIdentity.class);
                identity.set(preparedStatement, 1);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(1)));
                new Restrictions(NonHostAccount.get(host, identity), elements.getNonNullable(2)).set(preparedStatement, 3);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "agent_restrictions_ord (entity, stronger, weaker) VALUES (?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(4));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                IdentityImplementation.create(elements.getNonNullable(0)).castTo(InternalNonHostIdentity.class).set(preparedStatement, 1);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(1)));
                preparedStatement.setLong(3, Integer64Wrapper.decode(elements.getNonNullable(2)));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "client_agent (entity, agent, " + Commitment.COLUMNS + ", name) VALUES (?, ?, ?, ?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(5));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(4);
                IdentityImplementation.create(elements.getNonNullable(0)).castTo(InternalNonHostIdentity.class).set(preparedStatement, 1);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(1)));
                new Commitment(elements.getNonNullable(2)).set(preparedStatement, 3);
                preparedStatement.setString(6, StringWrapper.decodeNonNullable(elements.getNonNullable(3)));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "outgoing_role (entity, agent, relation, context) VALUES (?, ?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(6));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(4);
                final @Nonnull InternalNonHostIdentity identity = IdentityImplementation.create(elements.getNonNullable(0)).castTo(InternalNonHostIdentity.class);
                identity.set(preparedStatement, 1);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(1)));
                IdentityImplementation.create(elements.getNonNullable(2)).castTo(SemanticType.class).checkIsRoleType().set(preparedStatement, 3);
                Context.get(NonHostAccount.get(host, identity), elements.getNonNullable(3)).set(preparedStatement, 4);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "incoming_role (entity, issuer, relation, agent) VALUES (?, ?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(7));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(4);
                IdentityImplementation.create(elements.getNonNullable(0)).castTo(InternalNonHostIdentity.class).set(preparedStatement, 1);
                IdentityImplementation.create(elements.getNonNullable(1)).castTo(InternalNonHostIdentity.class).set(preparedStatement, 2);
                IdentityImplementation.create(elements.getNonNullable(2)).castTo(SemanticType.class).checkIsRoleType().set(preparedStatement, 3);
                preparedStatement.setLong(4, Integer64Wrapper.decode(elements.getNonNullable(3)));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    /* -------------------------------------------------- State Getter and Setter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code entry.agent.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_STATE_ENTRY = SemanticType.map("entry.agent.agents.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Agent.NUMBER, Agent.CLIENT, Agent.REMOVED);
    
    /**
     * Stores the semantic type {@code table.agent.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_STATE_TABLE = SemanticType.map("table.agent.agents.state@core.digitalid.net").load(ListWrapper.XDF_TYPE, AGENT_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.permission.agent.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_STATE_ENTRY = SemanticType.map("entry.permission.agent.agents.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Agent.NUMBER, FreezableAgentPermissions.TYPE);
    
    /**
     * Stores the semantic type {@code table.permission.agent.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_PERMISSION_STATE_TABLE = SemanticType.map("table.permission.agent.agents.state@core.digitalid.net").load(ListWrapper.XDF_TYPE, AGENT_PERMISSION_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.restrictions.agent.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_STATE_ENTRY = SemanticType.map("entry.restrictions.agent.agents.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Agent.NUMBER, Restrictions.TYPE);
    
    /**
     * Stores the semantic type {@code table.restrictions.agent.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType AGENT_RESTRICTIONS_STATE_TABLE = SemanticType.map("table.restrictions.agent.agents.state@core.digitalid.net").load(ListWrapper.XDF_TYPE, AGENT_RESTRICTIONS_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.client.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CLIENT_STATE_ENTRY = SemanticType.map("entry.client.agents.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Agent.NUMBER, Commitment.TYPE, Client.NAME);
    
    /**
     * Stores the semantic type {@code table.client.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CLIENT_STATE_TABLE = SemanticType.map("table.client.agents.state@core.digitalid.net").load(ListWrapper.XDF_TYPE, CLIENT_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.outgoing.role.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OUTGOING_ROLE_STATE_ENTRY = SemanticType.map("entry.outgoing.role.agents.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Agent.NUMBER, Role.RELATION, Context.TYPE);
    
    /**
     * Stores the semantic type {@code table.outgoing.role.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OUTGOING_ROLE_STATE_TABLE = SemanticType.map("table.outgoing.role.agents.state@core.digitalid.net").load(ListWrapper.XDF_TYPE, OUTGOING_ROLE_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code entry.incoming.role.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType INCOMING_ROLE_STATE_ENTRY = SemanticType.map("entry.incoming.role.agents.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Role.ISSUER, Role.RELATION, Role.AGENT);
    
    /**
     * Stores the semantic type {@code table.incoming.role.agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType INCOMING_ROLE_STATE_TABLE = SemanticType.map("table.incoming.role.agents.state@core.digitalid.net").load(ListWrapper.XDF_TYPE, INCOMING_ROLE_STATE_ENTRY);
    
    
    /**
     * Stores the semantic type {@code agents.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.map("agents.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, AGENT_STATE_TABLE, AGENT_PERMISSION_STATE_TABLE, AGENT_RESTRICTIONS_STATE_TABLE, CLIENT_STATE_TABLE, OUTGOING_ROLE_STATE_TABLE, INCOMING_ROLE_STATE_TABLE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException {
        if (agent == null) { throw new SQLException("The agent may not be null for state queries of the core service."); }
        
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String from = " FROM " + site + "agent_permission_order po, " + site + "agent_restrictions_ord ro, " + site;
        final @Nonnull String where = " t WHERE po.entity = " + entity + " AND po.stronger = " + agent + " AND ro.entity = " + entity + " AND ro.stronger = " + agent + " AND t.entity = " + entity + " AND po.weaker = t.agent AND ro.weaker = t.agent";
        
        final @Nonnull FreezableArray<Block> tables = FreezableArray.get(6);
        try (@Nonnull Statement statement = Database.createStatement()) {
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT agent, client, removed" + from + "agent" + where)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final long number = resultSet.getLong(1);
                    final boolean client = resultSet.getBoolean(2);
                    final boolean removed = resultSet.getBoolean(3);
                    entries.add(TupleWrapper.encode(AGENT_STATE_ENTRY, Integer64Wrapper.encode(Agent.NUMBER, number), BooleanWrapper.encode(Agent.CLIENT, client), BooleanWrapper.encode(Agent.REMOVED, removed)));
                }
                tables.set(0, ListWrapper.encode(AGENT_STATE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT agent, " + FreezableAgentPermissions.COLUMNS + from + "agent_permission" + where)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) { entries.add(TupleWrapper.encode(AGENT_PERMISSION_STATE_ENTRY, Integer64Wrapper.encode(Agent.NUMBER, resultSet.getLong(1)), FreezableAgentPermissions.getEmptyOrSingle(resultSet, 2))); }
                tables.set(1, ListWrapper.encode(AGENT_PERMISSION_STATE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT agent, " + Restrictions.COLUMNS + from + "agent_restrictions" + where)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) { entries.add(TupleWrapper.encode(AGENT_RESTRICTIONS_STATE_ENTRY, Integer64Wrapper.encode(Agent.NUMBER, resultSet.getLong(1)), Restrictions.get(entity, resultSet, 2))); }
                tables.set(2, ListWrapper.encode(AGENT_RESTRICTIONS_STATE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT agent, " + Commitment.COLUMNS + ", name" + from + "client_agent" + where)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final long number = resultSet.getLong(1);
                    final @Nonnull Commitment commitment = Commitment.get(resultSet, 2);
                    final @Nonnull String name = resultSet.getString(5);
                    entries.add(TupleWrapper.encode(CLIENT_STATE_ENTRY, Integer64Wrapper.encode(Agent.NUMBER, number), commitment, StringWrapper.encodeNonNullable(Client.NAME, name)));
                }
                tables.set(3, ListWrapper.encode(CLIENT_STATE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT agent, relation, context" + from + "outgoing_role" + where)) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final long number = resultSet.getLong(1);
                    final @Nonnull Identity relation = IdentityImplementation.getNotNull(resultSet, 2);
                    final @Nonnull Context context = Context.getNotNull(entity, resultSet, 3);
                    entries.add(TupleWrapper.encode(OUTGOING_ROLE_STATE_ENTRY, Integer64Wrapper.encode(Agent.NUMBER, number), relation.toBlockable(Role.RELATION), context));
                }
                tables.set(4, ListWrapper.encode(OUTGOING_ROLE_STATE_TABLE, entries.freeze()));
            }
            
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT issuer, relation, agent FROM " + site + "incoming_role WHERE entity = " + entity + " AND " + restrictions.isRole())) {
                final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
                while (resultSet.next()) {
                    final @Nonnull Identity issuer = IdentityImplementation.getNotNull(resultSet, 1);
                    final @Nonnull Identity relation = IdentityImplementation.getNotNull(resultSet, 2);
                    final long number = resultSet.getLong(3);
                    entries.add(TupleWrapper.encode(INCOMING_ROLE_STATE_ENTRY, issuer.toBlockable(Role.ISSUER), relation.toBlockable(Role.RELATION), Integer64Wrapper.encode(Role.AGENT, number)));
                }
                tables.set(5, ListWrapper.encode(INCOMING_ROLE_STATE_TABLE, entries.freeze()));
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
            Database.onInsertIgnore(statement, site + "agent", "entity", "agent");
            Database.onInsertIgnore(statement, site + "agent_permission", "entity", "agent", "type");
            Database.onInsertIgnore(statement, site + "agent_restrictions", "entity", "agent");
            Database.onInsertIgnore(statement, site + "client", "entity", "agent");
            Database.onInsertIgnore(statement, site + "outgoing_role", "entity", "agent");
            Database.onInsertIgnore(statement, site + "incoming_role", "entity", "issuer", "relation");
        }
        
        final @Nonnull ReadOnlyArray<Block> tables = TupleWrapper.decode(block).getNonNullableElements(6);
        final @Nonnull String prefix = "INSERT" + Database.getConfiguration().IGNORE() + " INTO " + site;
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "agent (entity, agent, client, removed) VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(0));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(0)));
                preparedStatement.setBoolean(3, BooleanWrapper.decode(elements.getNonNullable(1)));
                preparedStatement.setBoolean(4, BooleanWrapper.decode(elements.getNonNullable(2)));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "agent_permission (entity, agent, " + FreezableAgentPermissions.COLUMNS + ") VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(1));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(2);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(0)));
                new FreezableAgentPermissions(elements.getNonNullable(1)).checkIsSingle().setEmptyOrSingle(preparedStatement, 3);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "agent_restrictions (entity, agent, " + Restrictions.COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(2));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(2);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(0)));
                new Restrictions(entity, elements.getNonNullable(1)).set(preparedStatement, 3);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "client_agent (entity, agent, " + Commitment.COLUMNS + ", name) VALUES (?, ?, ?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(3));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(0)));
                new Commitment(elements.getNonNullable(1)).set(preparedStatement, 3);
                preparedStatement.setString(6, StringWrapper.decodeNonNullable(elements.getNonNullable(2)));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "outgoing_role (entity, agent, relation, context) VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(4));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                preparedStatement.setLong(2, Integer64Wrapper.decode(elements.getNonNullable(0)));
                IdentityImplementation.create(elements.getNonNullable(1)).castTo(SemanticType.class).checkIsRoleType().set(preparedStatement, 3);
                Context.get(entity, elements.getNonNullable(2)).set(preparedStatement, 4);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(prefix + "incoming_role (entity, issuer, relation, agent) VALUES (?, ?, ?, ?)")) {
            entity.set(preparedStatement, 1);
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(tables.getNonNullable(5));
            for (final @Nonnull Block entry : entries) {
                final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(entry).getNonNullableElements(3);
                IdentityImplementation.create(elements.getNonNullable(0)).castTo(InternalNonHostIdentity.class).set(preparedStatement, 2);
                IdentityImplementation.create(elements.getNonNullable(1)).castTo(SemanticType.class).checkIsRoleType().set(preparedStatement, 3);
                preparedStatement.setLong(4, Integer64Wrapper.decode(elements.getNonNullable(2)));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.onInsertNotIgnore(statement, site + "agent");
            Database.onInsertNotIgnore(statement, site + "agent_permission");
            Database.onInsertNotIgnore(statement, site + "agent_restrictions");
            Database.onInsertNotIgnore(statement, site + "client");
            Database.onInsertNotIgnore(statement, site + "outgoing_role");
            Database.onInsertNotIgnore(statement, site + "incoming_role");
        }
        
        redeterminePermissionsOrder(entity, null);
        redetermineRestrictionsOrder(entity, null);
        
        ClientAgent.reset(entity);
        OutgoingRole.reset(entity);
        
        if (entity instanceof Role) resetIncomingRoles((Role) { entity); }
    }
    
    @Override
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws DatabaseException {
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + site + "incoming_role WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "outgoing_role WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "client_agent WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "agent_restrictions_ord WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "agent_restrictions WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "agent_permission_order WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "agent_permission WHERE entity = " + entity);
            statement.executeUpdate("DELETE FROM " + site + "agent WHERE entity = " + entity);
        }
    }
    
    /* -------------------------------------------------- Agent -------------------------------------------------- */
    
    /**
     * Returns whether the given agent is removed.
     * 
     * @param agent the agent of interest.
     * 
     * @return whether the given agent is removed.
     */
    @Pure
    @NonCommitting
    static boolean isRemoved(@Nonnull Agent agent) throws DatabaseException {
        final @Nonnull String SQL = "SELECT removed FROM " + agent.getEntity().getSite() + "agent WHERE entity = " + agent.getEntity() + " AND agent = " + agent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return resultSet.getBoolean(1); }
            else { throw new SQLException("The given agent could not be found."); }
        }
    }
    
    /**
     * Adds the given agent.
     * 
     * @param agent the agent to be added.
     */
    private static void addAgent(@Nonnull Agent agent) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO " + agent.getEntity().getSite() + "agent (entity, agent, client, removed) VALUES (" + agent.getEntity() + ", " + agent + ", " + Database.toBoolean(agent.isClient()) + ", " + Database.toBoolean(agent.isRemoved()) + ")");
        }
    }
    
    /**
     * Removes the given agent.
     * 
     * @param agent the agent to be removed.
     */
    @NonCommitting
    static void removeAgent(@Nonnull Agent agent) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull String SQL = "UPDATE " + agent.getEntity().getSite() + "agent SET removed = " + Database.toBoolean(true) + " WHERE entity = " + agent.getEntity() + " AND agent = " + agent + " AND removed = " + Database.toBoolean(false);
            if (statement.executeUpdate(SQL) == 0) { throw new SQLException("The agent with the number " + agent + " of " + agent.getEntity().getIdentity().getAddress() + " could not be removed."); }
        }
    }
    
    /**
     * Unremoves the given agent.
     * 
     * @param agent the agent to be unremoved.
     */
    @NonCommitting
    static void unremoveAgent(@Nonnull Agent agent) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull String SQL = "UPDATE " + agent.getEntity().getSite() + "agent SET removed = " + Database.toBoolean(false) + " WHERE entity = " + agent.getEntity() + " AND agent = " + agent + " AND removed = " + Database.toBoolean(true);
            if (statement.executeUpdate(SQL) == 0) { throw new SQLException("The agent with the number " + agent + " of " + agent.getEntity().getIdentity().getAddress() + " could not be unremoved."); }
        }
    }
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Returns the permissions of the given agent.
     * 
     * @param agent the agent whose permissions are to be returned.
     * 
     * @return the permissions of the given agent.
     */
    @Pure
    @NonCommitting
    static @Capturable @Nonnull @NonFrozen FreezableAgentPermissions getPermissions(@Nonnull Agent agent) throws DatabaseException {
        final @Nonnull String SQL = "SELECT " + FreezableAgentPermissions.COLUMNS + " FROM " + agent.getEntity().getSite() + "agent_permission WHERE entity = " + agent.getEntity() + " AND agent = " + agent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            return FreezableAgentPermissions.get(resultSet, 1);
        }
    }
    
    /**
     * Adds the given permissions to the given agent.
     * 
     * @param agent the agent to which the permissions are to be added.
     * @param permissions the permissions to be added to the given agent.
     */
    @NonCommitting
    static void addPermissions(@Nonnull Agent agent, @Nonnull @Frozen ReadOnlyAgentPermissions permissions) throws DatabaseException {
        final @Nonnull String SQL = "INSERT INTO " + agent.getEntity().getSite() + "agent_permission (entity, agent, " + FreezableAgentPermissions.COLUMNS + ") VALUES (" + agent.getEntity() + ", " + agent + ", ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            permissions.set(preparedStatement, 1);
            preparedStatement.executeBatch();
        }
        redeterminePermissionsOrder(agent);
    }
    
    /**
     * Removes the given permissions from the given agent.
     * 
     * @param agent the agent from which the permissions are to be removed.
     * @param permissions the permissions to be removed from the given agent.
     */
    @NonCommitting
    static void removePermissions(@Nonnull Agent agent, @Nonnull @Frozen ReadOnlyAgentPermissions permissions) throws DatabaseException {
        final @Nonnull String SQL = "DELETE FROM " + agent.getEntity().getSite() + "agent_permission WHERE entity = " + agent.getEntity() + " AND agent = " + agent + " AND " + FreezableAgentPermissions.CONDITION;
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            permissions.set(preparedStatement, 1);
            final int[] counts = preparedStatement.executeBatch();
            for (final int count : counts) if (count < 1) { throw new SQLException("Could not find a particular permission."); }
        }
        redeterminePermissionsOrder(agent);
    }
    
    /* -------------------------------------------------- Permissions Order -------------------------------------------------- */
    
    /**
     * Redetermines which agents have stronger and weaker permissions than the given agent or, if that one is null, all agents at the given entity.
     * Please note that it is intentionally ignored whether agents have been removed. Make sure to check this at some other place!
     * 
     * @param entity the entity whose order of agent permissions is to be redetermined.
     * @param agent the agent whose order of permissions is to be redetermined or null.
     */
    @NonCommitting
    private static void redeterminePermissionsOrder(@Nonnull NonHostEntity entity, @Nullable Agent agent) throws DatabaseException {
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + site + "agent_permission_order WHERE entity = " + entity + (agent != null ? " AND (stronger = " + agent + " OR weaker = " + agent + ")": ""));
            statement.executeUpdate("INSERT INTO " + site + "agent_permission_order (entity, stronger, weaker) SELECT " + entity + ", ps.agent, pw.agent FROM " + site + "agent_permission pw, " + site + "agent_permission ps "
                    + "WHERE pw.entity = " + entity + " AND ps.entity = " + entity + (agent != null ? " AND (pw.agent = " + agent + " OR ps.agent = " + agent + ")": "") + " AND (pw.type = ps.type OR ps.type = " + FreezableAgentPermissions.GENERAL +") AND (NOT pw.type_writing OR ps.type_writing) "
                    + "GROUP BY pw.agent, ps.agent HAVING COUNT(*) = (SELECT COUNT(*) FROM " + site + "agent_permission p WHERE p.entity = " + entity + " AND p.agent = pw.agent)");
        }
    }
    
    /**
     * Redetermines which agents have stronger and weaker permissions than the given agent.
     * 
     * @param agent the agent whose order of permissions is to be redetermined.
     */
    @NonCommitting
    private static void redeterminePermissionsOrder(@Nonnull Agent agent) throws DatabaseException {
        redeterminePermissionsOrder(agent.getEntity(), agent);
    }
    
    /* -------------------------------------------------- Restrictions -------------------------------------------------- */
    
    /**
     * Returns the restrictions of the given agent.
     * 
     * @param agent the agent whose restrictions are to be returned.
     * 
     * @return the restrictions of the given agent.
     * 
     * @ensure return.match(agent) : "The returned restrictions match the given agent.";
     */
    @Pure
    @NonCommitting
    static @Nonnull Restrictions getRestrictions(@Nonnull Agent agent) throws DatabaseException {
        final @Nonnull String SQL = "SELECT " + Restrictions.COLUMNS + " FROM " + agent.getEntity().getSite() + "agent_restrictions WHERE entity = " + agent.getEntity() + " AND agent = " + agent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return Restrictions.get(agent.getEntity(), resultSet, 1).checkMatch(agent); }
            else { throw new SQLException("The given agent has no restrictions."); }
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Sets the restrictions of the given agent to the given restrictions.
     * 
     * @param agent the agent whose restrictions are to be set initially.
     * @param restrictions the restrictions to be set for the given agent.
     */
    @NonCommitting
    private static void setRestrictions(@Nonnull Agent agent, @Nonnull Restrictions restrictions) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO " + agent.getEntity().getSite() + "agent_restrictions (entity, agent, " + Restrictions.COLUMNS + ") VALUES (" + agent.getEntity() + ", " + agent + ", " + restrictions + ")");
        } catch (@Nonnull SQLException exception) {
            final @Nullable Contact contact = restrictions.getContact();
            if (contact != null && contact.getPerson().hasBeenMerged(exception)) { setRestrictions(agent, restrictions); }
            else { throw exception; }
        }
        redetermineRestrictionsOrder(agent);
    }
    
    /**
     * Replaces the restrictions of the given agent.
     * 
     * @param agent the agent whose restrictions are to be replaced.
     * @param oldRestrictions the old restrictions to be replaced with the new restrictions.
     * @param newRestrictions the new restrictions with which the old restrictions are replaced.
     * 
     * @throws DatabaseException if the passed restrictions are not the old restrictions.
     * 
     * @require oldRestrictions.match(agent) : "The old restrictions match the given agent.";
     * @require newRestrictions.match(agent) : "The new restrictions match the given agent.";
     */
    @NonCommitting
    static void replaceRestrictions(@Nonnull Agent agent, @Nonnull Restrictions oldRestrictions, @Nonnull Restrictions newRestrictions) throws DatabaseException {
        Require.that(oldRestrictions.match(agent)).orThrow("The old restrictions match the given agent.");
        Require.that(newRestrictions.match(agent)).orThrow("The new restrictions match the given agent.");
        
        final int count;
        try (@Nonnull Statement statement = Database.createStatement()) {
            count = statement.executeUpdate("UPDATE " + agent.getEntity().getSite() + "agent_restrictions SET " + newRestrictions.toUpdateValues() + " WHERE entity = " + agent.getEntity() + " AND agent = " + agent + " AND " + oldRestrictions.toUpdateCondition());
        } catch (@Nonnull SQLException exception) {
            final @Nullable Contact contact = newRestrictions.getContact();
            if (contact != null && contact.getPerson().hasBeenMerged(exception)) { replaceRestrictions(agent, oldRestrictions, newRestrictions); }
            return;
        }
        if (count == 0) {
            final @Nullable Contact contact = oldRestrictions.getContact();
            final @Nonnull SQLException exception = new SQLException("The restrictions of the agent with the number " + agent + " of " + agent.getEntity().getIdentity().getAddress() + " could not be replaced.");
            if (contact != null && contact.getPerson().hasBeenMerged(exception)) { replaceRestrictions(agent, oldRestrictions, newRestrictions); }
            return;
        }
        redetermineRestrictionsOrder(agent);
    }
    
    /* -------------------------------------------------- Restrictions Order -------------------------------------------------- */
    
    /**
     * Redetermines which agents have stronger and weaker restrictions than the given agent or, if that one is null, all agents at the given entity.
     * Please note that it is intentionally ignored whether agents have been removed. Make sure to check this at some other place!
     * 
     * @param entity the entity whose order of agent restrictions is to be redetermined.
     * @param agent the agent whose order of restrictions is to be redetermined or null.
     */
    @NonCommitting
    private static void redetermineRestrictionsOrder(@Nonnull NonHostEntity entity, @Nullable Agent agent) throws DatabaseException {
        final @Nonnull Site site = entity.getSite();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DELETE FROM " + site + "agent_restrictions_ord WHERE entity = " + entity + (agent != null ? " AND (stronger = " + agent + " OR weaker = " + agent + ")": ""));
            statement.executeUpdate("INSERT INTO " + site + "agent_restrictions_ord (entity, stronger, weaker) SELECT " + entity + ", rs.agent, rw.agent FROM " + site + "agent_restrictions rs, " + site + "agent_restrictions rw"
                    + " WHERE rs.entity = " + entity + " AND rw.entity = " + entity + (agent != null ? " AND (rs.agent = " + agent + " OR rw.agent = " + agent + ")": "")
                    + " AND (NOT rw.client OR rs.client) AND (NOT rw.role OR rs.role) AND (NOT rw.context_writing OR rs.context_writing)"
                    + " AND (rw.context IS NULL OR rs.context IS NOT NULL AND EXISTS (SELECT * FROM " + site + "context_subcontext cx WHERE cx.entity = " + entity + " AND cx.context = rs.context AND cx.subcontext = rw.context))" // TODO: Make sure to ignore unreachable contexts (i.e. every context is stronger than a context not reachable from the root)!
                    + " AND (rw.contact IS NULL OR rs.contact IS NOT NULL AND rw.contact = rs.contact OR rs.context IS NOT NULL AND EXISTS (SELECT * FROM " + site + "context_subcontext cx, " + site + "context_contact cc WHERE cx.entity = " + entity + " AND cx.context = rs.context AND cc.entity = " + entity + " AND cc.context = cx.subcontext AND cc.contact = rw.contact))"
                    + " AND (rw.client OR rs.context_writing AND rs.context IS NOT NULL AND EXISTS (SELECT * FROM " + site + "context_subcontext cx, " + site + "outgoing_role og WHERE cx.entity = " + entity + " AND cx.context = rs.context AND og.entity = " + entity + " AND og.agent = rw.agent AND cx.subcontext = og.context))");
        }
    }
    
    /**
     * Redetermines which agents have stronger and weaker restrictions than the given agent.
     * 
     * @param agent the agent whose order of restrictions is to be redetermined.
     */
    @NonCommitting
    private static void redetermineRestrictionsOrder(@Nonnull Agent agent) throws DatabaseException {
        redetermineRestrictionsOrder(agent.getEntity(), agent);
    }
    
    /**
     * Redetermines which agents have stronger and weaker restrictions than the given agent.
     * 
     * @param entity the entity whose order of agent restrictions is to be redetermined.
     */
    @NonCommitting
    static void redetermineRestrictionsOrder(@Nonnull NonHostEntity entity) throws DatabaseException {
        redetermineRestrictionsOrder(entity, null);
    }
    
    /* -------------------------------------------------- Covering -------------------------------------------------- */
    
    /**
     * Returns the agents that are weaker than the given agent.
     * 
     * @param agent the agent whose weaker agents are to be returned.
     * 
     * @return the agents that are weaker than the given agent.
     * 
     * @ensure return.!isFrozen() : "The list is not frozen.";
     */
    @Pure
    @NonCommitting
    static @Capturable @Nonnull FreezableList<Agent> getWeakerAgents(@Nonnull Agent agent) throws DatabaseException {
        final @Nonnull NonHostEntity entity = agent.getEntity();
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String SQL = "SELECT agent, client, removed FROM " + site + "agent_permission_order po, " + site + "agent_restrictions_ord ro, " + site + "agent ag WHERE po.entity = " + entity + " AND po.stronger = " + agent + " AND ro.entity = " + entity + " AND ro.stronger = " + agent + " AND ag.entity = " + entity + " AND po.weaker = ag.agent AND ro.weaker = ag.agent";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Agent> agents = new FreezableLinkedList<>();
            while (resultSet.next()) { agents.add(Agent.get(entity, resultSet, 1, 2, 3)); }
            return agents;
        }
    }
    
    /**
     * Returns the agent weaker than the given agent with the given agent number.
     * 
     * @param agent the agent whose weaker agent is to be returned.
     * @param agentNumber the number of the agent which is to be returned.
     * 
     * @return the agent weaker than the given agent with the given agent number.
     * 
     * @throws DatabaseException if no weaker agent with the given number is found.
     */
    @Pure
    @NonCommitting
    static @Nonnull Agent getWeakerAgent(@Nonnull Agent agent, long agentNumber) throws DatabaseException {
        final @Nonnull NonHostEntity entity = agent.getEntity();
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String SQL = "SELECT agent, client, removed FROM " + site + "agent_permission_order po, " + site + "agent_restrictions_ord ro, " + site + "agent ag WHERE po.entity = " + entity + " AND po.stronger = " + agent + " AND po.weaker = " + agentNumber + " AND ro.entity = " + entity + " AND ro.stronger = " + agent + " AND ro.weaker = " + agentNumber + " AND ag.entity = " + entity + " AND ag.agent = " + agentNumber;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return Agent.getNotNull(entity, resultSet, 1, 2, 3); }
            else { throw new SQLException("No weaker agent with the given number was found."); }
        }
    }
    
    /**
     * Returns whether the first agent is stronger than the second agent.
     * 
     * @param agent1 the first agent.
     * @param agent2 the second agent.
     * 
     * @return whether the first agent is stronger than the second agent.
     * 
     * @require agent1.getEntity().equals(agent2.getEntity()) : "Both agents belong to the same entity.";
     */
    @Pure
    @NonCommitting
    static boolean isStronger(@Nonnull Agent agent1, @Nonnull Agent agent2) throws DatabaseException {
        Require.that(agent1.getEntity().equals(agent2.getEntity())).orThrow("Both agents belong to the same entity.");
        
        final @Nonnull NonHostEntity entity = agent1.getEntity();
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String SQL = "SELECT EXISTS (SELECT * FROM " + site + "agent_permission_order po, " + site + "agent_restrictions_ord ro WHERE po.entity = " + entity + " AND po.stronger = " + agent1 + " AND po.weaker = " + agent2 + " AND ro.entity = " + entity + " AND ro.stronger = " + agent1 + " AND ro.weaker = " + agent2 + ")";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            return resultSet.next() && resultSet.getBoolean(1);
        }
    }
    
    /* -------------------------------------------------- Client Agent -------------------------------------------------- */
    
    /**
     * Adds the client agent with the given parameters to the database.
     * 
     * @param clientAgent the client agent which is to be added.
     * @param permissions the permissions of the client agent.
     * @param restrictions the restrictions of the client agent.
     * @param commitment the commitment of the client agent.
     * @param name the name of the given client agent.
     * 
     * @require permissions.isFrozen() : "The permissions are frozen.";
     * @require Client.isValid(name) : "The name is valid.";
     */
    @NonCommitting
    static void addClientAgent(@Nonnull ClientAgent clientAgent, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nonnull Commitment commitment, @Nonnull String name) throws DatabaseException {
        Require.that(permissions.isFrozen()).orThrow("The permissions are frozen.");
        Require.that(Client.isValidName(name)).orThrow("The name is valid.");
        
        addAgent(clientAgent);
        setRestrictions(clientAgent, restrictions);
        addPermissions(clientAgent, permissions);
        
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "INSERT INTO " + entity.getSite() + "client_agent (entity, agent, " + Commitment.COLUMNS + ", name) VALUES (?, ?, ?, ?, ?, ?)";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            entity.set(preparedStatement, 1);
            clientAgent.set(preparedStatement, 2);
            commitment.set(preparedStatement, 3);
            preparedStatement.setString(6, name);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Returns the client agent with the given commitment at the given entity or null if no such client agent is found.
     * 
     * @param entity the entity whose client agent is to be returned.
     * @param commitment the commitment of the client agent to be returned.
     * 
     * @return the client agent with the given commitment at the given entity or null if no such client agent is found.
     */
    @Pure
    @NonCommitting
    public static @Nullable ClientAgent getClientAgent(@Nonnull NonHostEntity entity, @Nonnull Commitment commitment) throws DatabaseException {
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String SQL = "SELECT a.agent, a.removed FROM " + site + "client_agent c, " + site + "agent a WHERE c.entity = " + entity + " AND a.entity = " + entity + " AND c.agent = a.agent AND " + Commitment.CONDITION;
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            commitment.set(preparedStatement, 1);
            try (@Nonnull ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) { return ClientAgent.get(entity, resultSet.getLong(1), resultSet.getBoolean(2)); }
                else { return null; }
            }
        } catch (SQLException exception) {
            throw DatabaseException.get(exception);
        }
    }
    
    /* -------------------------------------------------- Commitment -------------------------------------------------- */
    
    /**
     * Returns the commitment of the given client agent.
     * 
     * @param clientAgent the client agent whose commitment is to be returned.
     */
    @Pure
    @NonCommitting
    static @Nonnull Commitment getCommitment(@Nonnull ClientAgent clientAgent) throws DatabaseException {
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "SELECT " + Commitment.COLUMNS + " FROM " + entity.getSite() + "client_agent WHERE entity = " + entity + " AND agent = " + clientAgent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return Commitment.get(resultSet, 1); }
            else { throw new SQLException("The given client agent has no commitment."); }
        }
    }
    
    /**
     * Replaces the commitment of the given client agent.
     * 
     * @param clientAgent the client agent whose commitment is to be replaced.
     * @param oldCommitment the old commitment of the given client agent.
     * @param newCommitment the new commitment of the given client agent.
     */
    @NonCommitting
    static void replaceCommitment(@Nonnull ClientAgent clientAgent, @Nonnull Commitment oldCommitment, @Nonnull Commitment newCommitment) throws DatabaseException {
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "UPDATE " + entity.getSite() + "client_agent SET " + Commitment.UPDATE + " WHERE entity = " + entity + " AND agent = " + clientAgent + " AND " + Commitment.CONDITION;
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            newCommitment.set(preparedStatement, 1);
            oldCommitment.set(preparedStatement, 4);
            if (preparedStatement.executeUpdate() == 0) { throw new SQLException("The commitment of the client agent with the number " + clientAgent + " could not be replaced."); }
        }
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    /**
     * Returns the name of the given client agent.
     * 
     * @param clientAgent the client agent whose name is to be returned.
     * 
     * @ensure Client.isValid(return) : "The returned name is valid.";
     */
    @Pure
    @NonCommitting
    static @Nonnull String getName(@Nonnull ClientAgent clientAgent) throws DatabaseException {
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "SELECT name FROM " + entity.getSite() + "client_agent WHERE entity = " + entity + " AND agent = " + clientAgent;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) {
                final @Nonnull String name = resultSet.getString(1);
                if (!Client.isValidName(name)) { throw new SQLException("The name of the client agent with the number " + clientAgent + " is invalid."); }
                return name;
            } else { throw new SQLException("The given client agent has no name."); }
        }
    }
    
    /**
     * Replaces the name of the given client agent.
     * 
     * @param clientAgent the client agent whose name is to be replaced.
     * @param oldName the old name of the given client agent.
     * @param newName the new name of the given client agent.
     * 
     * @require Client.isValid(oldName) : "The old name is valid.";
     * @require Client.isValid(newName) : "The new name is valid.";
     */
    @NonCommitting
    static void replaceName(@Nonnull ClientAgent clientAgent, @Nonnull String oldName, @Nonnull String newName) throws DatabaseException {
        Require.that(Client.isValidName(oldName)).orThrow("The old name is valid.");
        Require.that(Client.isValidName(newName)).orThrow("The new name is valid.");
        
        final @Nonnull NonHostEntity entity = clientAgent.getEntity();
        final @Nonnull String SQL = "UPDATE " + entity.getSite() + "client_agent SET name = ? WHERE entity = " + entity + " AND agent = " + clientAgent + " AND name = ?";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            preparedStatement.setString(1, newName);
            preparedStatement.setString(2, oldName);
            if (preparedStatement.executeUpdate() == 0) { throw new SQLException("The name of the client agent with the number " + clientAgent + " could not be replaced."); }
        }
    }
    
    /* -------------------------------------------------- Outgoing Role -------------------------------------------------- */
    
    /**
     * Adds the outgoing role with the given parameters to the database.
     * 
     * @param outgoingRole the outgoing role which is to be added to the database.
     * @param relation the relation between the issuing and the receiving identity.
     * @param context the context to which the outgoing role is to be assigned.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     * @require context.getEntity().equals(outgoingRole.getEntity()) : "The context belongs to the entity of the outgoing role.";
     */
    @NonCommitting
    static void addOutgoingRole(@Nonnull OutgoingRole outgoingRole, @Nonnull SemanticType relation, @Nonnull Context context) throws DatabaseException {
        Require.that(relation.isRoleType()).orThrow("The relation is a role type.");
        Require.that(context.getEntity().equals(outgoingRole.getEntity())).orThrow("The context belongs to the entity of the outgoing role.");
        
        addAgent(outgoingRole);
        
        final @Nonnull NonHostEntity entity = outgoingRole.getEntity();
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO " + entity.getSite() + "outgoing_role (entity, agent, relation, context) VALUES (" + entity + ", " + outgoingRole + ", " + relation + ", " + context + ")");
        }
        
        setRestrictions(outgoingRole, Restrictions.MIN);
    }
    
    /**
     * Returns the outgoing role with the given relation at the given entity or null if no such role is found.
     * 
     * @param entity the entity whose outgoing role is to be returned.
     * @param relation the relation between the issuing and the receiving identity.
     * @param restrictable whether the outgoing role can be restricted.
     * 
     * @return the outgoing role with the given relation at the given entity or null if no such role is found.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    @Pure
    @NonCommitting
    public static @Nullable OutgoingRole getOutgoingRole(@Nonnull NonHostEntity entity, @Nonnull SemanticType relation, boolean restrictable) throws DatabaseException {
        Require.that(relation.isRoleType()).orThrow("The relation is a role type.");
        
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String SQL = "SELECT a.agent, a.removed FROM " + site + "outgoing_role o, " + site + "agent a WHERE o.entity = " + entity + " AND a.entity = " + entity + " AND o.agent = a.agent AND o.relation = " + relation;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return OutgoingRole.get(entity, resultSet.getLong(1), resultSet.getBoolean(2), restrictable); }
            else { return null; }
        }
    }
    
    /* -------------------------------------------------- Relation -------------------------------------------------- */
    
    /**
     * Returns the relation of the given outgoing role.
     * 
     * @param outgoingRole the outgoing role whose relation is to be returned.
     * 
     * @ensure return.isRoleType() : "The returned relation is a role type.";
     */
    @Pure
    @NonCommitting
    static @Nonnull SemanticType getRelation(@Nonnull OutgoingRole outgoingRole) throws DatabaseException {
        final @Nonnull NonHostEntity entity = outgoingRole.getEntity();
        final @Nonnull String SQL = "SELECT relation FROM " + entity.getSite() + "outgoing_role WHERE entity = " + entity + " AND agent = " + outgoingRole;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return IdentityImplementation.getNotNull(resultSet, 1).castTo(SemanticType.class).checkIsRoleType(); }
            else { throw new SQLException("The given outgoing role has no relation."); }
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
    }
    
    /**
     * Replaces the relation of the given outgoing role.
     * 
     * @param outgoingRole the outgoing role whose relation is to be replaced.
     * @param oldRelation the old relation of the given outgoing role.
     * @param newRelation the new relation of the given outgoing role.
     * 
     * @require oldRelation.isRoleType() : "The old relation is a role type.";
     * @require newRelation.isRoleType() : "The new relation is a role type.";
     */
    @NonCommitting
    static void replaceRelation(@Nonnull OutgoingRole outgoingRole, @Nonnull SemanticType oldRelation, @Nonnull SemanticType newRelation) throws DatabaseException {
        Require.that(oldRelation.isRoleType()).orThrow("The old relation is a role type.");
        Require.that(newRelation.isRoleType()).orThrow("The new relation is a role type.");
        
        final @Nonnull NonHostEntity entity = outgoingRole.getEntity();
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull String SQL = "UPDATE " + entity.getSite() + "outgoing_role SET relation = " + newRelation + " WHERE entity = " + entity + " AND agent = " + outgoingRole + " AND relation = " + oldRelation;
            if (statement.executeUpdate(SQL) == 0) { throw new SQLException("The relation of the client agent with the number " + outgoingRole + " could not be replaced."); }
        }
    }
    
    /* -------------------------------------------------- Context -------------------------------------------------- */
    
    /**
     * Returns the context of the given outgoing role.
     * 
     * @param outgoingRole the outgoing role whose context is to be returned.
     * 
     * @ensure return.getEntity().equals(outgoingRole.getEntity()) : "The context belongs to the same entity as the outgoing role.";
     */
    @Pure
    @NonCommitting
    static @Nonnull Context getContext(@Nonnull OutgoingRole outgoingRole) throws DatabaseException {
        final @Nonnull NonHostEntity entity = outgoingRole.getEntity();
        final @Nonnull String SQL = "SELECT context FROM " + entity.getSite() + "outgoing_role WHERE entity = " + entity + " AND agent = " + outgoingRole;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            if (resultSet.next()) { return Context.getNotNull(entity, resultSet, 1); }
            else { throw new SQLException("The given outgoing role has no context."); }
        }
    }
    
    /**
     * Replaces the context of the given outgoing role.
     * 
     * @param outgoingRole the outgoing role whose context is to be replaced.
     * @param oldContext the old context of the given outgoing role.
     * @param newContext the new context of the given outgoing role.
     * 
     * @require oldContext.isRoleType() : "The old context is a role type.";
     * @require newContext.isRoleType() : "The new context is a role type.";
     */
    @NonCommitting
    static void replaceContext(@Nonnull OutgoingRole outgoingRole, @Nonnull Context oldContext, @Nonnull Context newContext) throws DatabaseException {
        Require.that(oldContext.getEntity().equals(outgoingRole.getEntity())).orThrow("The old context belongs to the same entity as the outgoing role.");
        Require.that(newContext.getEntity().equals(outgoingRole.getEntity())).orThrow("The new context belongs to the same entity as the outgoing role.");
        
        final @Nonnull NonHostEntity entity = outgoingRole.getEntity();
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull String SQL = "UPDATE " + entity.getSite() + "outgoing_role SET context = " + newContext + " WHERE entity = " + entity + " AND agent = " + outgoingRole + " AND context = " + oldContext;
            if (statement.executeUpdate(SQL) == 0) { throw new SQLException("The context of the client agent with the number " + outgoingRole + " could not be replaced."); }
        }
        redetermineRestrictionsOrder(outgoingRole);
    }
    
    /* -------------------------------------------------- Incoming Role -------------------------------------------------- */
    
    /**
     * Adds the incoming role with the given issuer, relation and agent number to the given entity.
     * 
     * @param entity the entity to which the incoming role is to be added.
     * @param issuer the issuer of the incoming role.
     * @param relation the relation of the incoming role.
     * @param agentNumber the agent number of the incoming role.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    @NonCommitting
    static void addIncomingRole(@Nonnull NonHostEntity entity, @Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation, long agentNumber) throws DatabaseException {
        Require.that(relation.isRoleType()).orThrow("The relation is a role type.");
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO " + entity.getSite() + "incoming_role (entity, issuer, relation, agent) VALUES (" + entity + ", " + issuer + ", " + relation + ", " + agentNumber + ")");
        } catch (@Nonnull SQLException exception) {
            if (issuer.hasBeenMerged(exception)) { addIncomingRole(entity, issuer, relation, agentNumber); }
        }
    }
    
    /**
     * Removes the incoming role with the given issuer and relation at the given entity.
     * 
     * @param entity the entity from which the incoming role is to be removed.
     * @param issuer the issuer of the incoming role which is to be removed.
     * @param relation the relation of the incoming role which is to be removed.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    @NonCommitting
    static void removeIncomingRole(@Nonnull NonHostEntity entity, @Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation) throws DatabaseException {
        Require.that(relation.isRoleType()).orThrow("The relation is a role type.");
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            final @Nonnull String SQL = "DELETE FROM " + entity.getSite() + "incoming_role WHERE entity = " + entity + " AND issuer = " + issuer + " AND relation = " + relation;
            if (statement.executeUpdate(SQL) == 0) {
                final @Nonnull SQLException exception = new SQLException("The incoming role with the issuer " + issuer.getAddress() + " and relation " + relation.getAddress() + " could not be removed.");
                if (issuer.hasBeenMerged(exception)) { removeIncomingRole(entity, issuer, relation); }
            }
        }
    }
    
    /**
     * Resets the incoming roles of the given role.
     * 
     * @param role the role whose incoming roles are to be reset.
     */
    @NonCommitting
    static void resetIncomingRoles(@Nonnull Role role) throws DatabaseException {
        final @Nonnull ReadOnlyList<NonNativeRole> roles = role.getRoles();
        final @Nonnull HashSet<NonNativeRole> foundRoles = new HashSet<>();
        final @Nonnull String SQL = "SELECT issuer, relation, agent FROM " + role.getSite() + "incoming_role WHERE entity = " + role;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            while (resultSet.next()) {
                final @Nonnull InternalNonHostIdentity issuer = IdentityImplementation.getNotNull(resultSet, 1).castTo(InternalNonHostIdentity.class);
                final @Nonnull SemanticType relation = IdentityImplementation.getNotNull(resultSet, 2).castTo(SemanticType.class).checkIsRoleType();
                final long agentNumber = resultSet.getLong(3);
                
                boolean found = false;
                for (final @Nonnull NonNativeRole subrole : roles) {
                    if (subrole.getIssuer().equals(issuer) && relation.equals(subrole.getRelation())) {
                        foundRoles.add(subrole);
                        found = true;
                        break;
                    }
                }
                if (!found) { role.addRole(issuer, relation, agentNumber); }
            }
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Some values returned by the database are invalid.", exception);
        }
        for (final @Nonnull NonNativeRole subrole : roles) {
            if (!foundRoles.contains(subrole)) { subrole.remove(); }
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
